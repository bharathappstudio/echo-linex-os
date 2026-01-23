package org.echo.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

/* ================= AUTO SYSTEM THEME ================= */

data class NothingColors(
    val bg: Color,
    val panel: Color,
    val card: Color,
    val accent: Color,
    val accentSoft: Color,
    val text: Color,
    val muted: Color,
    val border: Color
)

private val DarkNothing = NothingColors(
    bg = Color(0xFF000000),
    panel = Color(0xFF0A0A0A),
    card = Color(0xFF0F0F0F),
    accent = Color(0xFF2EFF71),
    accentSoft = Color(0xFF1B5E3A),
    text = Color.White,
    muted = Color(0xFF9E9E9E),
    border = Color(0xFF1F1F1F)
)

private val LightNothing = NothingColors(
    bg = Color(0xFFF4F5F7),
    panel = Color.White,
    card = Color(0xFFF0F0F0),
    accent = Color(0xFF00C853),
    accentSoft = Color(0xFFB9F6CA),
    text = Color.Black,
    muted = Color(0xFF616161),
    border = Color(0xFFE0E0E0)
)

@Composable
fun NothingTheme(content: @Composable (NothingColors) -> Unit) {
    content(if (isSystemInDarkTheme()) DarkNothing else LightNothing)
}

/* ================= MAIN UI ================= */

@Composable
fun UserActivityUI(
    userName: String?,
    accessToken: String?,
    authClient: Any?,
    onBack: () -> Unit
) {
    NothingTheme { C ->

        var time by remember { mutableStateOf(LocalDateTime.now()) }

        LaunchedEffect(Unit) {
            while (true) {
                time = LocalDateTime.now()
                delay(1000)
            }
        }

        val formatter = remember {
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm:ss", Locale.ENGLISH)
        }

        Row(
            Modifier
                .fillMaxSize()
                .background(C.bg)
        ) {

            /* -------- LEFT PANEL -------- */
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(C.panel)
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("NOTHING / ENERGY", fontSize = 11.sp, color = C.muted)
                        Text(
                            "Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = C.text
                        )
                    }

                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(C.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userName?.take(1)?.uppercase() ?: "U",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Energy\nOverview",
                    fontSize = 42.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = C.text
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    time.format(formatter),
                    fontSize = 12.sp,
                    color = C.muted
                )

                Spacer(Modifier.height(32.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NothingStat("Earnings", "34.6", "AUD", C)
                    NothingStat("Savings", "5,137", "km", C)
                    NothingStat("Energy", "83", "%", C)
                }

                Spacer(Modifier.height(28.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        NothingCard(
                            "Current Power",
                            "4.71 kW",
                            Modifier.weight(1.3f),
                            "BAR",
                            C
                        )
                        NothingCard(
                            "Balance",
                            "3.41 kW",
                            Modifier.weight(1f),
                            "LINE",
                            C
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        NothingCard("Battery", "300 kW", Modifier.weight(1f), "GAUGE", C)
                        NothingCard("Savings", "100.1 kW", Modifier.weight(1f), "ARC", C)
                    }
                }

                Spacer(Modifier.height(36.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, C.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = C.text
                    )
                ) {
                    Text("EXIT SYSTEM", letterSpacing = 1.sp)
                }
            }

            /* -------- RIGHT PANEL -------- */
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .background(C.bg)
            ) {

                DesktopWebView("https://bharathappstudio.github.io/Echo-Web/")

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(28.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(C.card.copy(0.92f))
                        .border(1.dp, C.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = C.accent
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "LIVE INTERFACE CONNECTED",
                            fontSize = 11.sp,
                            color = C.text,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

/* ================= COMPONENTS ================= */

@Composable
fun NothingStat(label: String, value: String, unit: String, C: NothingColors) {
    Column {
        Text(label, fontSize = 11.sp, color = C.muted)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = C.text
            )
            Spacer(Modifier.width(4.dp))
            Text(unit, fontSize = 11.sp, color = C.muted)
        }
    }
}

@Composable
fun NothingCard(
    title: String,
    value: String,
    modifier: Modifier,
    type: String,
    C: NothingColors
) {
    Surface(
        modifier = modifier
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = C.card
    ) {
        Column(Modifier.padding(18.dp)) {

            Text(title, fontSize = 12.sp, color = C.muted)
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = C.text
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                when (type) {
                    "BAR" -> Row(
                        Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        repeat(12) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(Random.nextFloat())
                                    .background(C.accent, RoundedCornerShape(6.dp))
                            )
                        }
                    }

                    "GAUGE" -> Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(C.accentSoft, CircleShape)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.7f)
                                .fillMaxHeight()
                                .background(C.accent, CircleShape)
                        )
                    }

                    "ARC" -> Canvas(Modifier.fillMaxSize()) {
                        drawArc(
                            color = C.accent,
                            startAngle = 180f,
                            sweepAngle = 140f,
                            useCenter = false,
                            style = Stroke(width = 8f)
                        )
                    }

                    "LINE" -> Canvas(Modifier.fillMaxSize()) {
                        drawLine(
                            C.accent,
                            start = Offset(0f, size.height * 0.7f),
                            end = Offset(size.width, size.height * 0.3f),
                            strokeWidth = 6f
                        )
                    }
                }
            }
        }
    }
}
