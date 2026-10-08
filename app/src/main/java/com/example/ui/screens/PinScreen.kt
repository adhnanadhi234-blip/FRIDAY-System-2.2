package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationIntensity
import com.example.service.HapticSoundManager
import com.example.ui.components.FridayHudPanel
import com.example.ui.components.FridayKeypad
import com.example.ui.components.ScanlineAndGridBackground
import com.example.ui.theme.LocalFridayColors

@Composable
fun PinScreen(
    isFirstLaunch: Boolean,
    onPinSuccess: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    hapticSoundManager: HapticSoundManager,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    animationIntensity: AnimationIntensity = AnimationIntensity.MEDIUM
) {
    val hudColors = LocalFridayColors.current

    var currentPinInput by remember { mutableStateOf("") }
    var confirmPinTarget by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var stepMessage by remember {
        mutableStateOf(
            if (isFirstLaunch) "CREATE PIN" else "ENTER SECURITY PIN"
        )
    }

    val maxPinDigits = 6

    fun onDigit(digit: String) {
        if (currentPinInput.length < maxPinDigits) {
            hapticSoundManager.playClick(soundEnabled, hapticEnabled)
            currentPinInput += digit
            errorMessage = null
        }
    }

    fun onDelete() {
        if (currentPinInput.isNotEmpty()) {
            hapticSoundManager.playClick(soundEnabled, hapticEnabled)
            currentPinInput = currentPinInput.dropLast(1)
            errorMessage = null
        }
    }

    fun onClear() {
        hapticSoundManager.playClick(soundEnabled, hapticEnabled)
        currentPinInput = ""
        errorMessage = null
    }

    fun onEnter() {
        if (currentPinInput.length < 4) {
            hapticSoundManager.playError(soundEnabled, hapticEnabled)
            errorMessage = "MINIMUM 4 DIGITS REQUIRED"
            return
        }

        if (isFirstLaunch) {
            if (confirmPinTarget == null) {
                // First step complete: save target and ask for confirmation
                hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
                confirmPinTarget = currentPinInput
                currentPinInput = ""
                stepMessage = "CONFIRM PIN"
                errorMessage = null
            } else {
                // Confirm step
                if (currentPinInput == confirmPinTarget) {
                    hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
                    onPinSuccess(currentPinInput)
                } else {
                    hapticSoundManager.playError(soundEnabled, hapticEnabled)
                    errorMessage = "PIN MISMATCH // RE-ENTER INITIAL PIN"
                    confirmPinTarget = null
                    currentPinInput = ""
                    stepMessage = "CREATE PIN"
                }
            }
        } else {
            // Normal authentication
            val valid = onVerifyPin(currentPinInput)
            if (valid) {
                hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
                onPinSuccess(currentPinInput)
            } else {
                hapticSoundManager.playError(soundEnabled, hapticEnabled)
                errorMessage = "ACCESS DENIED // INVALID SECURITY PIN"
                currentPinInput = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(hudColors.background)
            .testTag("pin_screen")
    ) {
        ScanlineAndGridBackground(intensity = animationIntensity)

        // Landscape Row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Security Telemetry & PIN Indicators
            FridayHudPanel(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxSize()
                    .padding(end = 12.dp),
                title = if (isFirstLaunch) "SECURITY INITIALIZATION" else "SYSTEM ACCESS VERIFICATION",
                statusText = "CRYPTOGRAPHIC GATE"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FRIDAY SECURITY PROTOCOL",
                        color = hudColors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stepMessage,
                        color = hudColors.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // PIN Mask Indicators (Holographic Dots)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until maxPinDigits) {
                            val filled = i < currentPinInput.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (filled) hudColors.primary else hudColors.border.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp,
                                        if (filled) hudColors.accent else hudColors.border.copy(alpha = 0.4f),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = hudColors.warning,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "SHA-256 SALT ENCRYPTED // LOCAL ONLY",
                            color = hudColors.textMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Right Side: Futuristic Keypad
            FridayHudPanel(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxSize(),
                title = "TACTICAL NUMERIC KEYPAD",
                statusText = "SECURE INPUT"
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    FridayKeypad(
                        onDigitPress = { onDigit(it) },
                        onDeletePress = { onDelete() },
                        onClearPress = { onClear() },
                        onEnterPress = { onEnter() }
                    )
                }
            }
        }
    }
}
