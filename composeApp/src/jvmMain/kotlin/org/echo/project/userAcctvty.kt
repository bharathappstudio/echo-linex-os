package org.echo.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.Key.Companion.R
// REMOVED: import androidx.compose.ui.input.key.Key.Companion.R <--- THIS WAS THE ERROR
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

/* --- API CONFIG --- */
private const val GEMINI_API_KEY = "AIzaSyChSAUK_AQCJX692iAYF8tdpz08i_L5Lmo"
private const val MODEL_ID = "gemini-2.5-flash"
private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_ID:generateContent?key=$GEMINI_API_KEY"

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun UserActivityUI(userName: String?, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Style Palette
    val textMain = Color(0xFF111827)
    val textMuted = Color(0xFF6B7280)
    val borderLight = Color(0xFFE5E7EB)
    val orangeBrand = Color(0xFFF97316)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Image(
            // In Compose Multiplatform, use the generated Res object
            painter = painterResource(Res.drawable.bb),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                color = Color.Black.copy(alpha = 0.3f),
                blendMode = BlendMode.Darken
            )
        )

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* TOP NAVIGATION & PROMO */
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textMain)
                }

                Surface(
                    modifier = Modifier.align(Alignment.Center).padding(top = 8.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFFFF7ED).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFFFFEDD5))
                ) {
                }
            }

            /* CHAT AREA */
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg, textMain, borderLight)
                    }
                }
            }

            /* INPUT BOX */
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(0.1f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, borderLight),
                color = Color.White.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp, color = textMain),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) Text("Ask Lovart to create...", color = textMuted.copy(0.5f))
                            innerTextField()
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AttachFile, null, tint = textMuted, modifier = Modifier.size(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Lightbulb, null, tint = textMuted, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Outlined.Language, null, tint = textMuted, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(16.dp))
                            Box(
                                modifier = Modifier.size(32.dp).background(if(inputText.isNotBlank()) Color.Black else borderLight, CircleShape)
                                    .clickable(enabled = inputText.isNotBlank()) {
                                        val userTxt = inputText
                                        messages.add(ChatMessage(userTxt, true))
                                        inputText = ""
                                        scope.launch {
                                            val response = fetchGemini(userTxt)
                                            messages.add(ChatMessage(response, false))
                                            listState.animateScrollToItem(messages.size - 1)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowUpward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            /* TOOL CHIPS */
            Row(
                modifier = Modifier.padding(vertical = 20.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(Modifier.width(20.dp))
                ToolChip("Nano Banana Pro", Icons.Default.AutoAwesome, true)
                ToolChip("Design", Icons.Outlined.Image)
                ToolChip("Branding", Icons.Outlined.StarOutline)
                ToolChip("Illustration", Icons.Outlined.Brush)
                ToolChip("Video", Icons.Outlined.PlayCircle)
                Spacer(Modifier.width(20.dp))
            }
        }
    }
}

@Composable
fun ToolChip(label: String, icon: ImageVector, isOrange: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if(isOrange) Color(0xFFFDBA74) else Color(0xFFE5E7EB)),
        color = if(isOrange) Color(0xFFFFF7ED).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.8f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if(isOrange) Color(0xFFF97316) else Color(0xFF6B7280), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, color = if(isOrange) Color(0xFFF97316) else Color(0xFF374151), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage, textColor: Color, borderColor: Color) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if(msg.isUser) Alignment.End else Alignment.Start) {
        Surface(
            color = if(msg.isUser) Color(0xFFF9FAFB).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.85f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Text(msg.text, modifier = Modifier.padding(12.dp), color = textColor, fontSize = 15.sp)
        }
    }
}

suspend fun fetchGemini(prompt: String): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("contents", JSONArray().put(JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        }))
    }
    val request = Request.Builder().url(GEMINI_URL).post(json.toString().toRequestBody("application/json".toMediaType())).build()
    try {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            JSONObject(body).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
        }
    } catch (e: Exception) { "AI Error: ${e.localizedMessage}" }
}