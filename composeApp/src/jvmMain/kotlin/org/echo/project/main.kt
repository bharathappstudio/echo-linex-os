package org.echo.project

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import org.json.JSONObject
import java.awt.BorderLayout
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.JPanel

/* ---------- DYNAMIC THEME HELPERS ---------- */
@Composable
fun getThemeColors() = if (isSystemInDarkTheme()) {
    ThemePalette(
        bg = Color(0xFF000000),
        surface = Color(0xFF1E1E1E),
        text = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF808080),
        border = Color.White.copy(alpha = 0.1f)
    )
} else {
    ThemePalette(
        bg = Color(0xFFF5F5F5),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF000000),
        textSecondary = Color(0xFF606060),
        border = Color.Black.copy(alpha = 0.1f)
    )
}

data class ThemePalette(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val border: Color
)

/* ---------- SESSION MANAGER ---------- */
object SessionManager {
    private val sessionFile = File(System.getProperty("user.home"), ".echo_session.json")
    fun saveSession(name: String, email: String, photoUrl: String?) {
        val json = JSONObject().apply {
            put("name", name); put("email", email); put("photoUrl", photoUrl ?: ""); put("isLoggedIn", true)
        }
        sessionFile.writeText(json.toString())
    }
    fun loadSession(): Triple<String, String, String?>? {
        if (!sessionFile.exists()) return null
        return try {
            val json = JSONObject(sessionFile.readText())
            if (json.optBoolean("isLoggedIn", false)) {
                Triple(json.getString("name"), json.getString("email"), json.optString("photoUrl").takeIf { it.isNotEmpty() })
            } else null
        } catch (e: Exception) { null }
    }
    fun clearSession() {
        if (sessionFile.exists()) {
            val json = JSONObject(sessionFile.readText())
            json.put("isLoggedIn", false)
            sessionFile.writeText(json.toString())
        }
    }
}

/* ---------- MAIN ---------- */
@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    LaunchedEffect(Unit) { try { Platform.startup {} } catch (_: Exception) {} }
    val windowState = rememberWindowState(size = DpSize(1000.dp, 700.dp), position = WindowPosition(Alignment.Center))

    Window(onCloseRequest = ::exitApplication, state = windowState, title = "ECHO_OS", undecorated = true) {
        val theme = getThemeColors()
        Surface(modifier = Modifier.fillMaxSize(), color = theme.bg) {
            MaterialTheme(colorScheme = if(isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Column {
                    WindowDraggableArea { TitleBar(windowState, theme) { exitApplication() } }
                    Box(modifier = Modifier.weight(1f)) { App(theme) }
                }
            }
        }
    }
}

/* ---------- TITLE BAR ---------- */
@Composable
fun TitleBar(windowState: WindowState, theme: ThemePalette, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            // Change theme.bg to Transparent or a faded version
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                Spacer(Modifier.width(12.dp))
                Text(
                    "ECHO-APP",
                    style = TextStyle(
                        color = theme.text.copy(0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
            Row {
                TitleBarButton("—", theme) { windowState.isMinimized = true }
                TitleBarButton("▢", theme) {
                    windowState.placement = if (windowState.placement == WindowPlacement.Maximized)
                        WindowPlacement.Floating else WindowPlacement.Maximized
                }
                TitleBarButton("✕", theme, true) { onClose() }
            }
        }
    }
}

@Composable
fun TitleBarButton(text: String, theme: ThemePalette, isClose: Boolean = false, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(modifier = Modifier.size(32.dp).clickable(onClick = onClick).hoverable(interactionSource), contentAlignment = Alignment.Center) {
        Text(text, style = TextStyle(color = if (isClose && isHovered) Color.Red else theme.text, fontSize = 14.sp))
    }
}

/* ---------- APP NAVIGATION ---------- */
/* ... Keep all your imports and Theme/Session helpers the same ... */

@Composable
fun App(theme: ThemePalette) {
    val initialSession = remember { SessionManager.loadSession() }
    var currentScreen by remember { mutableStateOf("MAIN") }

    // --- State for Auth and Navigation ---
    var userName by remember { mutableStateOf<String?>(initialSession?.first) }
    var userEmail by remember { mutableStateOf<String?>(initialSession?.second) }
    var userPhotoUrl by remember { mutableStateOf<String?>(initialSession?.third) }
    var googleAccessToken by remember { mutableStateOf<String?>(null) } // New State
    var loggedIn by remember { mutableStateOf(initialSession != null) }

    // Initialize AuthClient once at the App level
    val googleAuthClient = remember {
        GoogleAuthClient { success, n, e, p, token -> // Updated to receive token
            if (success) {
                userName = n
                userEmail = e
                userPhotoUrl = p
                googleAccessToken = token
                loggedIn = true
                SessionManager.saveSession(n, e, p)
            }
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { screen ->
        when (screen) {
            "MAIN" -> MainUI(
                theme = theme,
                loggedIn = loggedIn,
                name = userName,
                email = userEmail,
                photo = userPhotoUrl,
                authClient = googleAuthClient, // Pass AuthClient
                onLoginSuccess = { n, e, p -> /* State handled by authClient callback */ },
                onLogout = {
                    loggedIn = false
                    userName = null
                    googleAccessToken = null
                    currentScreen = "MAIN"
                },
                onNextClick = { currentScreen = "USER_ACTIVITY" }
            )

            "USER_ACTIVITY" -> UserActivityUI(
                userName = userName,
                onBack = { currentScreen = "MAIN" }
            )
        }
    }
}

/* ---------- MAIN UI (Updated Signature) ---------- */
@Composable
fun MainUI(
    theme: ThemePalette,
    loggedIn: Boolean,
    name: String?,
    email: String?,
    photo: String?,
    authClient: GoogleAuthClient, // New Argument
    onLoginSuccess: (String, String, String?) -> Unit,
    onLogout: () -> Unit,
    onNextClick: () -> Unit
) {
    var webUrl by remember { mutableStateOf("https://bharathappstudio.github.io/Echo-Web/") }
    var emailInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        // Grid background effect (Same as your code)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gap = 40.dp.toPx()
            for (x in 0..size.width.toInt() step gap.toInt()) {
                for (y in 0..size.height.toInt() step gap.toInt()) {
                    drawCircle(theme.text.copy(0.05f), radius = 1.dp.toPx(), center = Offset(x.toFloat(), y.toFloat()))
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Box(modifier = Modifier.width(380.dp).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(theme.surface).border(1.dp, theme.border, RoundedCornerShape(16.dp)).padding(32.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    if (!loggedIn) {
                        LoginView(theme, emailInput, { emailInput = it },
                            onGoogle = { authClient.signIn() }, // Uses the passed client
                            onGithub = { webUrl = "https://echob.framer.ai" },
                            onContinue = {
                                if (emailInput.isNotBlank()) {
                                    val n = emailInput.substringBefore("@")
                                    onLoginSuccess(n, emailInput, null)
                                    SessionManager.saveSession(n, emailInput, null)
                                }
                            }
                        )
                    } else {
                        ProfileView(theme, name, email, photo, onNextClick)
                    }

                    if (loggedIn) {
                        Text("LOG-OUT", modifier = Modifier.clickable {
                            SessionManager.clearSession()
                            onLogout()
                        }.padding(vertical = 8.dp), style = TextStyle(fontFamily = FontFamily.Monospace, color = Color(0xFFFF5C5C), fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
            Spacer(Modifier.width(20.dp))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().border(1.dp, theme.border, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(theme.surface)) {
                DesktopWebView(webUrl)
            }
        }
    }
}

/* ... Keep all your helper views (LoginView, ProfileView, NothingButton, etc.) exactly as they were ... */

/* ---------- VIEWS ---------- */
@Composable
fun LoginView(theme: ThemePalette, email: String, onEmailChange: (String) -> Unit, onGoogle: () -> Unit, onGithub: () -> Unit, onContinue: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ECHO", style = TextStyle(fontSize = 42.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 8.sp, color = theme.text))
        Text("V.01 CORE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = theme.textSecondary)
        Spacer(Modifier.height(40.dp))
        NothingButton("Continue With Google", false, theme, onGoogle)
        Spacer(Modifier.height(12.dp))
        NothingButton("echo-studio-13", false, theme, onGithub)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            placeholder = { Text("EMAIL_ADDRESS", color = theme.textSecondary, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = theme.text, unfocusedTextColor = theme.text, focusedBorderColor = theme.text, unfocusedBorderColor = theme.border)
        )
        Spacer(Modifier.height(16.dp))
        NothingButton("ACCESS_CORE", true, theme, onContinue)
    }
}

@Composable
fun ProfileView(theme: ThemePalette, name: String?, email: String?, photoUrl: String?, onNext: () -> Unit) {
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(photoUrl) {
        if (!photoUrl.isNullOrEmpty()) {
            try { imageBitmap = ImageIO.read(URL(photoUrl)).toComposeImageBitmap() } catch (e: Exception) {}
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("USER_PROFILE", style = TextStyle(fontFamily = FontFamily.Monospace, color = theme.textSecondary, fontSize = 10.sp))
        Spacer(Modifier.height(30.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(theme.text.copy(0.05f)).border(1.dp, theme.border, CircleShape), contentAlignment = Alignment.Center) {
                if (imageBitmap != null) Image(bitmap = imageBitmap!!, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Box(modifier = Modifier.size(15.dp).clip(CircleShape).background(theme.textSecondary))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(name?.uppercase() ?: "UNKNOWN", style = TextStyle(color = theme.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                Text(email ?: "NO_MAIL", style = TextStyle(color = theme.textSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace))
            }
        }
        Spacer(Modifier.height(30.dp))
        Box(modifier = Modifier.fillMaxWidth().border(1.dp, theme.border, RoundedCornerShape(8.dp)).padding(16.dp)) {
            Column {
                Text("STATUS: AUTHENTICATED", color = Color.Green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(4.dp))
                Text("ENCRYPTION: ECHO-STUDIO-13", color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(Modifier.height(30.dp))
        NothingButton("NEXT", true, theme, onNext)
    }
}

@Composable
fun NothingButton(text: String, isPrimary: Boolean, theme: ThemePalette, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(if (isPrimary) theme.text else Color.Transparent).border(1.dp, theme.text, RoundedCornerShape(8.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = if (isPrimary) theme.bg else theme.text, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DesktopWebView(url: String) {
    val jfxPanel = remember { JFXPanel() }
    LaunchedEffect(url) {} // Trigger for future needs
    SwingPanel(modifier = Modifier.fillMaxSize(), factory = {
        JPanel(BorderLayout()).apply {
            add(jfxPanel, BorderLayout.CENTER)
            Platform.runLater {
                val wv = WebView()
                jfxPanel.scene = Scene(wv)
                wv.engine.load(url)
            }
        }
    })
}