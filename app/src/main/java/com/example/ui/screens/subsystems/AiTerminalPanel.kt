package com.example.ui.screens.subsystems

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FridayAiService
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors
import kotlinx.coroutines.launch

data class AiMessage(
    val sender: String, // "OPERATOR" or "FRIDAY"
    val text: String,
    val isSystem: Boolean = false
)

@Composable
fun AiTerminalPanel(
    aiService: FridayAiService,
    systemContextSummary: String,
    onClosePanel: () -> Unit
) {
    val hudColors = LocalFridayColors.current
    val scope = rememberCoroutineScope()

    var inputPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            AiMessage(
                sender = "FRIDAY",
                text = "NEURAL COGNITION INITIALIZED. STATUS: ${aiService.getAiStatus()}.\nAWAITING OPERATOR DIRECTIVES.",
                isSystem = true
            )
        )
    }

    fun submitQuery(promptText: String) {
        if (promptText.isBlank() || isThinking) return
        val query = promptText.trim()
        messages.add(AiMessage("OPERATOR", query))
        inputPrompt = ""
        isThinking = true

        scope.launch {
            val response = aiService.queryAi(query, systemContextSummary)
            messages.add(AiMessage("FRIDAY", response))
            isThinking = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_terminal_panel")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GEMINI AI TACTICAL BRAIN INTERFACE",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = aiService.getAiStatus(),
                    color = if (aiService.isOnlineAiAvailable()) hudColors.success else hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            FridayHudButton(
                text = "RETURN",
                testTag = "btn_return_ai",
                onClick = onClosePanel,
                modifier = Modifier.width(80.dp)
            )
        }

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("DIAGNOSTIC SCAN", "SECURITY AUDIT", "SYSTEM STATUS", "WHO ARE YOU?").forEach { chip ->
                FridayHudButton(
                    text = chip,
                    enabled = !isThinking,
                    onClick = { submitQuery(chip) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Conversation Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(hudColors.panelBg)
                .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { msg ->
                val isOp = msg.sender == "OPERATOR"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isOp) hudColors.border.copy(alpha = 0.15f) else hudColors.primary.copy(alpha = 0.1f))
                        .border(1.dp, if (isOp) hudColors.border.copy(alpha = 0.3f) else hudColors.primary.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "[${msg.sender}]",
                        color = if (isOp) hudColors.accent else hudColors.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = msg.text,
                        color = hudColors.textPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (isThinking) {
                item {
                    Text(
                        text = "FRIDAY IS SYNTHESIZING NEURAL RESPONSE...",
                        color = hudColors.accent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it.uppercase() },
                placeholder = { Text("ENTER COMMAND FOR FRIDAY...", color = hudColors.textMuted, fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = hudColors.textPrimary,
                    unfocusedTextColor = hudColors.textPrimary,
                    focusedBorderColor = hudColors.primary,
                    unfocusedBorderColor = hudColors.border
                )
            )

            FridayHudButton(
                text = "TRANSMIT",
                isActive = true,
                enabled = inputPrompt.isNotBlank() && !isThinking,
                onClick = { submitQuery(inputPrompt) },
                modifier = Modifier.width(110.dp)
            )
        }
    }
}
