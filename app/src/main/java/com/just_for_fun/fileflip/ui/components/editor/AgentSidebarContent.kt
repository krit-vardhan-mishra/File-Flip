package com.just_for_fun.fileflip.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.just_for_fun.fileflip.R
import com.just_for_fun.fileflip.ui.screens.SettingsState
import com.just_for_fun.fileflip.ui.theme.LocalAppColors
import com.just_for_fun.fileflip.ui.util.BackgroundDark
import com.just_for_fun.fileflip.ui.util.DividerColor
import com.just_for_fun.fileflip.ui.util.PrimaryBlue
import com.just_for_fun.fileflip.ui.util.SurfaceDark
import com.just_for_fun.fileflip.ui.util.TextGray
import com.just_for_fun.fileflip.ui.util.TextWhite
import com.just_for_fun.fileflip.ui.util.extractCodeBlocks
import com.just_for_fun.fileflip.ui.viewmodels.EditorViewModel

@Composable
fun AgentSidebarContent(
    viewModel: EditorViewModel,
    navController: NavController,
    onClose: () -> Unit,
    speakText: (String, String) -> Unit,
    speakingMessageId: String?,
    launchSpeechToText: ((String) -> Unit) -> Unit,
    onTextPatchSelected: (String) -> Unit
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAgentLoading by viewModel.isAgentLoading.collectAsState()
    val agentError by viewModel.agentError.collectAsState()

    var promptInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Auto-scroll to bottom of chat when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        // --- Sidebar Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FlipFile Agent",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                val apiKey = SettingsState.apiKey
                val modelName = when {
                    apiKey.startsWith("AIza") -> "Gemini 1.5 Flash"
                    apiKey.startsWith("gsk_") -> "Groq (Llama 3)"
                    apiKey.isNotEmpty() -> "OpenRouter (Gemini 2.5)"
                    else -> "No provider configured"
                }
                Text(
                    text = modelName,
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            if (chatMessages.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearChatHistory() }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Clear Chat",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        androidx.compose.material3.HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

        // --- Sidebar Body ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (SettingsState.apiKey.isEmpty()) {
                // Warning State: API Key Not Configured
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.error_3),
                        contentDescription = "Error Illustration",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI Capabilities Offline",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To use the AI coding & writing agent, please configure your API Key in Settings.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            onClose()
                            navController.navigate("settings")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Configure API Key", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (agentError != null) {
                // Error State with provided agent_error image
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.error_3),
                        contentDescription = "Error Illustration",
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Something went wrong",
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = agentError ?: "",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.clearChatHistory() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextGray),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear")
                        }
                        Button(
                            onClick = {
                                val lastUserPrompt = chatMessages.lastOrNull { it.role == "user" }?.content
                                if (lastUserPrompt != null) {
                                    viewModel.sendAgentPrompt(lastUserPrompt)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (chatMessages.isEmpty()) {
                // Welcome Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryBlue.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "How can I help you today?",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "I have access to the currently open file. You can ask me to write code, review sections, translate text, or create outlines.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Conversation message list
                androidx.compose.foundation.lazy.LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(chatMessages.size) { index ->
                        val message = chatMessages[index]
                        val isUser = message.role == "user"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (!isUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .background(
                                            color = if (isUser) PrimaryBlue.copy(alpha = 0.15f) else SurfaceDark.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isUser) PrimaryBlue.copy(alpha = 0.3f) else LocalAppColors.current.border,
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = message.content,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    // Parse and render action buttons for assistant messages
                                    if (!isUser) {
                                        val codeBlocks = extractCodeBlocks(message.content)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // TTS Play/Stop Button
                                            val isSpeakingThis = speakingMessageId == message.id
                                            IconButton(
                                                onClick = { speakText(message.id, message.content) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSpeakingThis) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                                    contentDescription = "Speak Text",
                                                    tint = if (isSpeakingThis) LocalAppColors.current.iconEmerald else TextGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Apply Code blocks button if any exist
                                            if (codeBlocks.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { onTextPatchSelected(codeBlocks.first()) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Code,
                                                        contentDescription = "Apply to Editor",
                                                        tint = PrimaryBlue,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "Apply Patch",
                                                    color = PrimaryBlue,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.clickable { onTextPatchSelected(codeBlocks.first()) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (isAgentLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = PrimaryBlue,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thinking...", color = TextGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- Sidebar Footer (Input Panel) ---
        if (SettingsState.apiKey.isNotEmpty() && agentError == null) {
            androidx.compose.material3.HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ask FlipFile Agent...", color = TextGray, fontSize = 13.sp) },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = LocalAppColors.current.border,
                        cursorColor = PrimaryBlue
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            launchSpeechToText { spokenText ->
                                promptInput = if (promptInput.isEmpty()) spokenText else "$promptInput $spokenText"
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = "Voice Input",
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            viewModel.sendAgentPrompt(promptInput)
                            promptInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryBlue, RoundedCornerShape(12.dp)),
                    enabled = !isAgentLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = BackgroundDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}