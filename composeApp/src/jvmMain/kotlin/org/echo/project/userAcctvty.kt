package org.echo.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin_app_echo.composeapp.generated.resources.Res
import kotlin_app_echo.composeapp.generated.resources.bb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.compose.resources.painterResource
import org.json.JSONArray
import org.json.JSONObject

/* --- GITHUB MODELS CONFIG --- */
private const val GITHUB_TOKEN = "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"
private const val MODEL_ID = "gpt-4o"
private const val GITHUB_URL = "https://models.github.ai/inference/chat/completions"

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun UserActivityUI(userName: String?, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Colors
    val textMain = Color(0xFF1F2937)
    val textMuted = Color(0xFF6B7280)
    val glassBackground = Color.White.copy(alpha = 0.75f)
    val accentColor = Color(0xFF3B82F6)

    val sendMessage = {
        if (inputText.isNotBlank()) {
            val messageToSend = inputText.trim()
            messages.add(ChatMessage(messageToSend, true))
            inputText = ""

            scope.launch {
                // Auto-scroll to user message
                listState.animateScrollToItem(messages.size - 1)

                val response = fetchGitHubGPT4(messageToSend)
                messages.add(ChatMessage(response, false))

                // Auto-scroll to AI response
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        /* BACKGROUND IMAGE */
        Image(
            painter = painterResource(Res.drawable.bb),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* TOP BAR */
            Box(modifier = Modifier.fillMaxWidth().padding(80.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textMain)
                }
            }

            /* CHAT AREA */
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 180.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { msg -> ChatBubble(msg, textMain, Color(0xFFE5E7EB)) }
                }
            }

            /* LIGHT GLASS INPUT BAR */
            Surface(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .widthIn(min = 400.dp, max = 550.dp)
                    .fillMaxWidth(0.75f)
                    .height(125.dp),
                shape = RoundedCornerShape(32.dp),
                color = glassBackground,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                "Ask Echo Ai",
                                color = textMuted.copy(alpha = 0.60f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(color = textMain, fontSize = 18.sp),
                            cursorBrush = SolidColor(accentColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onPreviewKeyEvent {
                                    // Handle "Enter" key on Desktop
                                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                        sendMessage()
                                        true
                                    } else false
                                }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = textMain, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .clickable { }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tune, null, tint = textMain, modifier = Modifier.size(18.dp))
                                Text("Tools", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { }.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Fast", color = textMuted, fontSize = 14.sp)
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = textMuted, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (inputText.isNotBlank()) textMain else Color.Black.copy(alpha = 0.1f))
                                    .clickable { if (inputText.isNotBlank()) sendMessage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (inputText.isNotBlank()) Icons.Default.ArrowUpward else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (inputText.isNotBlank()) Color.White else textMain,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage, textColor: Color, borderColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (msg.isUser) Color(0xFFE5E7EB) else Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 1.dp
        ) {
            Text(msg.text, modifier = Modifier.padding(12.dp), color = textColor, fontSize = 15.sp)
        }
    }
}

/* --- API LOGIC --- */
suspend fun fetchGitHubGPT4(prompt: String): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("model", MODEL_ID)
        put("messages", JSONArray().put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        }))
    }

    val request = Request.Builder()
        .url(GITHUB_URL)
        .addHeader("Authorization", "Bearer $GITHUB_TOKEN")
        .addHeader("Content-Type", "application/json")
        .post(json.toString().toRequestBody("application/json".toMediaType()))
        .build()

    try {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                JSONObject(body).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            } else "Error ${response.code}: $body"
        }
    } catch (e: Exception) { "Error: ${e.localizedMessage}" }
}
