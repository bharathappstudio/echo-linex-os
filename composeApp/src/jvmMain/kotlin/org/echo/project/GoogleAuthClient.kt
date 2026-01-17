package org.echo.project

import java.awt.Desktop
import java.net.*
import java.net.HttpURLConnection
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class GoogleAuthClient(
    private val onResult: (
        success: Boolean,
        name: String,
        email: String,
        photoUrl: String?
    ) -> Unit
) {

    companion object {
        private const val CLIENT_ID =
            "29905288838-q9g0gadb81h2us80hctppjbrs9oc9b0t.apps.googleusercontent.com"

        private const val CLIENT_SECRET =
            "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"

        private const val REDIRECT_URI = "http://localhost:8080"
        private const val SCOPE = "openid email profile"
    }

    private var server: HttpServer? = null

    fun signIn() {
        startServer()
        openBrowser()
    }

    /* ---------- OPEN GOOGLE LOGIN ---------- */
    private fun openBrowser() {
        val authUrl =
            "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=$CLIENT_ID" +
                    "&response_type=code" +
                    "&scope=${URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)}" +
                    "&redirect_uri=$REDIRECT_URI" +
                    "&prompt=consent"

        Desktop.getDesktop().browse(URI(authUrl))
    }

    /* ---------- LOCAL CALLBACK SERVER ---------- */
    private fun startServer() {
        server?.stop(0)

        server = HttpServer.create(InetSocketAddress(8080), 0).apply {
            createContext("/") { exchange ->
                val query = exchange.requestURI.query
                val codeParam = query
                    ?.split("&")
                    ?.firstOrNull { it.startsWith("code=") }
                    ?.substringAfter("code=")

                if (codeParam != null) {
                    val code = URLDecoder.decode(codeParam, StandardCharsets.UTF_8)

                    CoroutineScope(Dispatchers.IO).launch {
                        handleCode(code)
                    }

                    val response = "✅ Login successful! You can close this window."
                    exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                    exchange.responseBody.use { it.write(response.toByteArray()) }

                } else {
                    val response = "❌ Login failed"
                    exchange.sendResponseHeaders(400, response.toByteArray().size.toLong())
                    exchange.responseBody.use { it.write(response.toByteArray()) }
                }

                stop(0)
            }
            start()
        }
    }

    /* ---------- EXCHANGE CODE ---------- */
    private suspend fun handleCode(code: String) {
        try {
            val accessToken = getAccessToken(code)
            val profile = fetchUserInfo(accessToken)

            val name = profile.getString("name")
            val email = profile.getString("email")
            val photoUrl = profile.optString("picture", null)

            // ✅ MUST UPDATE UI ON SWING MAIN THREAD
            withContext(Dispatchers.Swing) {
                onResult(true, name, email, photoUrl)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Swing) {
                onResult(false, "", "", null)
            }
        }
    }

    /* ---------- TOKEN REQUEST ---------- */
    private fun getAccessToken(code: String): String {
        val url = URL("https://oauth2.googleapis.com/token")

        val postData =
            "code=$code" +
                    "&client_id=$CLIENT_ID" +
                    "&client_secret=$CLIENT_SECRET" +
                    "&redirect_uri=$REDIRECT_URI" +
                    "&grant_type=authorization_code"

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }

        conn.outputStream.use { it.write(postData.toByteArray()) }

        val response = conn.inputStream.bufferedReader().readText()
        return JSONObject(response).getString("access_token")
    }

    /* ---------- USER INFO ---------- */
    private fun fetchUserInfo(accessToken: String): JSONObject {
        val url = URL("https://www.googleapis.com/oauth2/v3/userinfo")

        val conn = (url.openConnection() as HttpURLConnection).apply {
            setRequestProperty("Authorization", "Bearer $accessToken")
        }

        val response = conn.inputStream.bufferedReader().readText()
        return JSONObject(response)
    }
}
