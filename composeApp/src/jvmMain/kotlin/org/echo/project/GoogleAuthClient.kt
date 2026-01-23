package org.echo.project

import java.awt.Desktop
import java.net.*
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.json.JSONObject
import org.json.JSONArray
import java.nio.charset.StandardCharsets

class GoogleAuthClient(
    private val onResult: (success: Boolean, name: String, email: String, photoUrl: String?, accessToken: String?) -> Unit
) {
    companion object {
        private const val CLIENT_ID = "115457911924-471511fqiu14iaibbsbvp8hfgiu5akei.apps.googleusercontent.com"
        private const val CLIENT_SECRET = "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"
        private const val REDIRECT_URI = "http://localhost:8080"
        // Added Calendar Scope
        private const val SCOPE = "openid email profile https://www.googleapis.com/auth/calendar.readonly"
    }

    private var server: HttpServer? = null

    fun signIn() {
        startServer()
        openBrowser()
    }

    private fun openBrowser() {
        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=$CLIENT_ID" +
                "&response_type=code" +
                "&scope=${URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)}" +
                "&redirect_uri=$REDIRECT_URI" +
                "&prompt=consent"
        Desktop.getDesktop().browse(URI(authUrl))
    }

    private fun startServer() {
        server?.stop(0)
        server = HttpServer.create(InetSocketAddress(8080), 0).apply {
            createContext("/") { exchange ->
                val query = exchange.requestURI.query
                val codeParam = query?.split("&")?.firstOrNull { it.startsWith("code=") }?.substringAfter("code=")
                if (codeParam != null) {
                    val code = URLDecoder.decode(codeParam, StandardCharsets.UTF_8)
                    CoroutineScope(Dispatchers.IO).launch { handleCode(code) }
                    val response = "✅ Login successful! Return to app."
                    exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                    exchange.responseBody.use { it.write(response.toByteArray()) }
                }
                stop(1)
            }
            start()
        }
    }

    private suspend fun handleCode(code: String) {
        try {
            val accessToken = getAccessToken(code)
            val profile = fetchUserInfo(accessToken)
            withContext(Dispatchers.Swing) {
                onResult(true, profile.getString("name"), profile.getString("email"), profile.optString("picture", null), accessToken)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Swing) { onResult(false, "", "", null, null) }
        }
    }

    private fun getAccessToken(code: String): String {
        val conn = (URL("https://oauth2.googleapis.com/token").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        val postData = "code=$code&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&redirect_uri=$REDIRECT_URI&grant_type=authorization_code"
        conn.outputStream.use { it.write(postData.toByteArray()) }
        return JSONObject(conn.inputStream.bufferedReader().readText()).getString("access_token")
    }

    private fun fetchUserInfo(accessToken: String): JSONObject {
        val conn = (URL("https://www.googleapis.com/oauth2/v3/userinfo").openConnection() as HttpURLConnection).apply {
            setRequestProperty("Authorization", "Bearer $accessToken")
        }
        return JSONObject(conn.inputStream.bufferedReader().readText())
    }

    // --- FETCH CALENDAR EVENTS ---
    fun fetchCalendarEvents(accessToken: String, callback: (List<CalendarEvent>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events?maxResults=10&orderBy=startTime&singleEvents=true")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                }
                val data = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONArray("items")
                val events = mutableListOf<CalendarEvent>()
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val start = item.getJSONObject("start").optString("dateTime", item.getJSONObject("start").optString("date"))
                    events.add(CalendarEvent(item.getString("summary"), start))
                }
                withContext(Dispatchers.Swing) { callback(events) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

data class CalendarEvent(val title: String, val time: String)