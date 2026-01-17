package org.echo.project

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.Group
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.web.WebView
import java.awt.BorderLayout
import javax.swing.JPanel

/* ---------- COLORS ---------- */
private val AppBg = Color(0xFFFFFFFF)
private val CardBg = Color.White
private val ButtonBlack = Color.Black
private val InputFill = Color(0xFFFFFFFF)
private val TextMain = Color(0xFF020617)
private val TextGray = Color(0xFF64748B)
private val DangerRed = Color(0xFFDC2626)

fun main() = application {
    remember { JFXPanel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Echo Desktop",
        state = rememberWindowState(placement = WindowPlacement.Maximized)
    ) {
        MaterialTheme { App() }
    }
}

@Composable
fun App() {
    var webUrl by remember { mutableStateOf("https://authentic-slide-917706.framer.app/") }
    var loading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf<String?>(null) }
    var profileImage by remember { mutableStateOf<String?>(null) }
    var loggedIn by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp)
    ) {

        /* ---------- LEFT PANEL (SMOOTH MODERN UI) ---------- */
        Box(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(CardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.weight(1f))

                AnimatedContent(
                    targetState = loggedIn,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "login-state"
                ) { isLogged ->

                    if (!isLogged) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text("Echo", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Text("Sign in to continue", fontSize = 14.sp, color = TextGray)

                            Spacer(modifier = Modifier.height(36.dp))

                            AnimatedContent(
                                targetState = loading,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "loading"
                            ) { isLoading ->
                                if (isLoading) {
                                    CircularProgressIndicator(color = ButtonBlack)
                                } else {
                                    Column {

                                        AuthButton("Continue with Google") {
                                            loading = true
                                            GoogleAuthClient { success, name, mail, photoUrl ->
                                                if (success) {
                                                    userName = name
                                                    email = mail
                                                    profileImage = photoUrl
                                                    loggedIn = true
                                                }
                                                loading = false
                                            }.signIn()
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        AuthButton("Continue with GitHub") {
                                            loading = true
                                            webUrl = "https://github.com/login"
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            HorizontalDivider(
                                                modifier = Modifier.weight(1f),
                                                color = Color(0xFFE2E8F0)
                                            )
                                            Text("  OR  ", fontSize = 12.sp, color = TextGray)
                                            HorizontalDivider(
                                                modifier = Modifier.weight(1f),
                                                color = Color(0xFFE2E8F0)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        TextField(
                                            value = email,
                                            onValueChange = { email = it },
                                            placeholder = { Text("you@example.com") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = InputFill,
                                                unfocusedContainerColor = InputFill,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        AuthButton("Continue") {
                                            if (email.isNotBlank()) {
                                                userName = email.substringBefore("@")
                                                loggedIn = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {

                        /* ---------- LOGGED IN UI ---------- */
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            DesktopProfileImage(
                                imageUrl = profileImage,
                                userName = userName,
                                size = 72
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Welcome 👋", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(userName ?: "", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(email, fontSize = 13.sp, color = TextGray)

                            Spacer(modifier = Modifier.height(32.dp))

                            AuthButton("Next") {
                                webUrl = "https://authentic-slide-917706.framer.app/"
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (loggedIn) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DangerRed)
                            .clickable {
                                loggedIn = false
                                userName = null
                                email = ""
                                profileImage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Logout", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        /* ---------- RIGHT WEBVIEW ---------- */
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
        ) {
            DesktopWebView(webUrl) { loading = false }
        }
    }
}

/* ---------- MODERN DESKTOP PROFILE IMAGE ---------- */
@Composable
fun DesktopProfileImage(
    imageUrl: String?,
    userName: String?,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(InputFill),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl == null) {
            Text(
                text = userName?.firstOrNull()?.uppercase() ?: "U",
                fontSize = (size / 2.4f).sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
        } else {
            SwingPanel(
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                factory = {
                    val panel = JPanel(BorderLayout())
                    val jfxPanel = JFXPanel()
                    panel.add(jfxPanel, BorderLayout.CENTER)
                    Platform.runLater {
                        val image = Image(imageUrl, size.toDouble(), size.toDouble(), true, true, true)
                        val view = ImageView(image)
                        jfxPanel.scene = Scene(Group(view))
                    }
                    panel
                }
            )
        }
    }
}

@Composable
fun DesktopWebView(url: String, onLoaded: () -> Unit) {
    val jfxPanel = remember { JFXPanel() }

    SwingPanel(
        modifier = Modifier.fillMaxSize(),
        factory = {
            JPanel(BorderLayout()).apply {
                add(jfxPanel, BorderLayout.CENTER)
                Platform.runLater {
                    val webView = WebView()
                    webView.engine.load(url)
                    jfxPanel.scene = Scene(webView)
                    onLoaded()
                }
            }
        }
    )
}

@Composable
private fun AuthButton(text: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(ButtonBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    pressed = true
                    onClick()
                    pressed = false
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
