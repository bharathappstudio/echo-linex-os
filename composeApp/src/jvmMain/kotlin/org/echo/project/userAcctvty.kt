package org.echo.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserActivityUI(theme: ThemePalette, userName: String?, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(theme.bg).padding(24.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CORE_ACTIVITY_LOG",
                        style = TextStyle(
                            color = theme.text,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "OPERATOR: ${userName?.uppercase() ?: "UNKNOWN"}",
                        style = TextStyle(color = theme.textSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                }

                // Minimal Back Button in corner
                Box(modifier = Modifier.width(180.dp)) {
                    NothingButton("RETURN_TO_DASH", false, theme, onBack)
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = theme.border)
            Spacer(Modifier.height(24.dp))

            // --- ACTIVITY CONTENT ---
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Column: Stats Cards
                Column(modifier = Modifier.weight(0.6f)) {
                    ActivityCard(theme, "SYSTEM_RUNTIME", "04:12:09:SS")
                    Spacer(Modifier.height(16.dp))
                    ActivityCard(theme, "DATA_ENCRYPTION", "AES-256 ACTIVE")
                    Spacer(Modifier.height(16.dp))
                    ActivityCard(theme, "NETWORK_STATUS", "SECURE_TUNNEL_ESTABLISHED")
                }

                Spacer(Modifier.width(20.dp))

                // Right Column: Decorative Log Terminal
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("LIVE_FEED", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        val logs = listOf(
                            "> Initializing ECHO_CORE...",
                            "> Protocol 13 established.",
                            "> Handshake complete.",
                            "> Fetching user data...",
                            "> Access granted: ${userName ?: "User"}",
                            "> Tracking activity...",
                            "> System nominal."
                        )
                        logs.forEach { log ->
                            Text(
                                text = log,
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(theme: ThemePalette, label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surface)
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(color = theme.textSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = TextStyle(color = theme.text, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            )
        }
    }
}