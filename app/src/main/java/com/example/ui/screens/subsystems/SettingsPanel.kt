package com.example.ui.screens.subsystems

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationIntensity
import com.example.model.FridaySettings
import com.example.model.FridayThemeId
import com.example.ui.components.FridayHudButton
import com.example.ui.components.HudTelemetryRow
import com.example.ui.theme.LocalFridayColors

@Composable
fun SettingsPanel(
    settings: FridaySettings,
    onSaveSettings: (FridaySettings) -> Unit,
    onRequestChangePin: () -> Unit,
    onClosePanel: () -> Unit
) {
    val hudColors = LocalFridayColors.current

    var currentTheme by remember { mutableStateOf(settings.theme) }
    var currentAnim by remember { mutableStateOf(settings.animationIntensity) }
    var soundEnabled by remember { mutableStateOf(settings.soundEffectsEnabled) }
    var hapticEnabled by remember { mutableStateOf(settings.hapticFeedbackEnabled) }
    var customApiKey by remember { mutableStateOf(settings.customGeminiApiKey) }

    fun commitSettings() {
        onSaveSettings(
            settings.copy(
                theme = currentTheme,
                animationIntensity = currentAnim,
                soundEffectsEnabled = soundEnabled,
                hapticFeedbackEnabled = hapticEnabled,
                customGeminiApiKey = customApiKey.trim()
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_panel")
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
                    text = "FRIDAY SYSTEM PARAMETERS & CONFIGURATION",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "THEME: ${currentTheme.name} // ANIMATIONS: ${currentAnim.name}",
                    color = hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            FridayHudButton(
                text = "RETURN",
                testTag = "btn_return_settings",
                onClick = onClosePanel,
                modifier = Modifier.width(80.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(hudColors.panelBg)
                        .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "HOLOGRAPHIC COLOR MATRIX",
                        color = hudColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FridayThemeId.entries.forEach { t ->
                            val isSel = currentTheme == t
                            val label = when (t) {
                                FridayThemeId.CYAN -> "CYAN HUD"
                                FridayThemeId.CRIMSON_GOLD -> "MARK 85"
                                FridayThemeId.EMERALD -> "EMERALD"
                                FridayThemeId.AMBER -> "AMBER"
                                FridayThemeId.VIOLET -> "VIOLET"
                            }
                            FridayHudButton(
                                text = label,
                                isActive = isSel,
                                onClick = {
                                    currentTheme = t
                                    commitSettings()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Animation Intensity Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(hudColors.panelBg)
                        .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "HUD ANIMATION INTENSITY",
                        color = hudColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimationIntensity.entries.forEach { intensity ->
                            val isSel = currentAnim == intensity
                            FridayHudButton(
                                text = intensity.name,
                                isActive = isSel,
                                onClick = {
                                    currentAnim = intensity
                                    commitSettings()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Audio & Haptics Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(hudColors.panelBg)
                            .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "AUDIO & HAPTIC FEEDBACK",
                            color = hudColors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FridayHudButton(
                                text = if (soundEnabled) "AUDIO: ON" else "AUDIO: OFF",
                                isActive = soundEnabled,
                                onClick = {
                                    soundEnabled = !soundEnabled
                                    commitSettings()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FridayHudButton(
                                text = if (hapticEnabled) "HAPTIC: ON" else "HAPTIC: OFF",
                                isActive = hapticEnabled,
                                onClick = {
                                    hapticEnabled = !hapticEnabled
                                    commitSettings()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Security Action
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(hudColors.panelBg)
                            .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "SECURITY ACCESS KEYS",
                            color = hudColors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FridayHudButton(
                            text = "CHANGE SECURITY PIN",
                            onClick = onRequestChangePin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Gemini AI API Key Input (Optional)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(hudColors.panelBg)
                        .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "GEMINI AI BRAIN CONFIGURATION (OPTIONAL)",
                        color = hudColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LEAVE EMPTY TO OPERATE IN OFFLINE TACTICAL AI MODE",
                        color = hudColors.textMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customApiKey,
                            onValueChange = { customApiKey = it },
                            placeholder = { Text("PASTE GEMINI API KEY...", color = hudColors.textMuted, fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = hudColors.textPrimary,
                                unfocusedTextColor = hudColors.textPrimary,
                                focusedBorderColor = hudColors.primary,
                                unfocusedBorderColor = hudColors.border.copy(alpha = 0.4f)
                            )
                        )
                        FridayHudButton(
                            text = "SAVE KEY",
                            isActive = true,
                            onClick = { commitSettings() },
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }
            }

            // About System
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(hudColors.panelBg)
                        .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "ABOUT FRIDAY SYSTEM",
                        color = hudColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HudTelemetryRow("VERSION", "MK-85.4.2 [RELEASE]")
                    HudTelemetryRow("FRAMEWORK", "NATIVE JETPACK COMPOSE // KOTLIN")
                    HudTelemetryRow("BUILD ENVIRONMENT", "ANDROIDIDE COMPATIBLE")
                    HudTelemetryRow("COGNITIVE SUBSYSTEM", "OFFLINE HEURISTIC + GEMINI AI READY")
                }
            }
        }
    }
}
