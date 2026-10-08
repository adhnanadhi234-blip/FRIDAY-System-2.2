package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.FridayRepository
import com.example.model.FridaySettings
import com.example.service.FridayAiService
import com.example.service.HapticSoundManager
import com.example.service.SystemAnalysisManager
import com.example.service.VoiceRecorderManager
import com.example.ui.screens.BootScreen
import com.example.ui.screens.MainHudScreen
import com.example.ui.screens.PinScreen
import com.example.ui.theme.FridaySystemTheme

enum class FridayAppState {
    BOOT,
    SECURITY_PIN,
    MAIN_HUD
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: FridayRepository
    private lateinit var systemAnalysisManager: SystemAnalysisManager
    private lateinit var voiceRecorderManager: VoiceRecorderManager
    private lateinit var aiService: FridayAiService
    private lateinit var hapticSoundManager: HapticSoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = FridayRepository(this)
        systemAnalysisManager = SystemAnalysisManager(this)
        voiceRecorderManager = VoiceRecorderManager(this)
        hapticSoundManager = HapticSoundManager(this)

        aiService = FridayAiService(
            customApiKeyProvider = { repository.getSettings().customGeminiApiKey }
        )

        setContent {
            FridayAppRoot(
                repository = repository,
                systemAnalysisManager = systemAnalysisManager,
                voiceRecorderManager = voiceRecorderManager,
                aiService = aiService,
                hapticSoundManager = hapticSoundManager
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceRecorderManager.release()
        hapticSoundManager.release()
    }
}

@Composable
fun FridayAppRoot(
    repository: FridayRepository,
    systemAnalysisManager: SystemAnalysisManager,
    voiceRecorderManager: VoiceRecorderManager,
    aiService: FridayAiService,
    hapticSoundManager: HapticSoundManager
) {
    var appState by remember { mutableStateOf(FridayAppState.BOOT) }
    var settings by remember { mutableStateOf(repository.getSettings()) }
    var isChangingPinMode by remember { mutableStateOf(false) }

    FridaySystemTheme(themeId = settings.theme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AnimatedContent(
                targetState = appState,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "app_state_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { state ->
                when (state) {
                    FridayAppState.BOOT -> {
                        BootScreen(
                            onBootComplete = {
                                appState = FridayAppState.SECURITY_PIN
                            },
                            animationIntensity = settings.animationIntensity
                        )
                    }

                    FridayAppState.SECURITY_PIN -> {
                        val isFirstLaunch = !repository.hasSecurityPin() || isChangingPinMode
                        PinScreen(
                            isFirstLaunch = isFirstLaunch,
                            onPinSuccess = { newOrVerifiedPin ->
                                if (isFirstLaunch) {
                                    repository.setSecurityPin(newOrVerifiedPin)
                                    isChangingPinMode = false
                                }
                                appState = FridayAppState.MAIN_HUD
                            },
                            onVerifyPin = { pin ->
                                repository.verifySecurityPin(pin)
                            },
                            hapticSoundManager = hapticSoundManager,
                            soundEnabled = settings.soundEffectsEnabled,
                            hapticEnabled = settings.hapticFeedbackEnabled,
                            animationIntensity = settings.animationIntensity
                        )
                    }

                    FridayAppState.MAIN_HUD -> {
                        MainHudScreen(
                            repository = repository,
                            systemAnalysisManager = systemAnalysisManager,
                            voiceRecorderManager = voiceRecorderManager,
                            aiService = aiService,
                            hapticSoundManager = hapticSoundManager,
                            settings = settings,
                            onSettingsChanged = { updated ->
                                settings = updated
                            },
                            onRequestChangePin = {
                                isChangingPinMode = true
                                appState = FridayAppState.SECURITY_PIN
                            },
                            onSecurityReset = {
                                appState = FridayAppState.SECURITY_PIN
                            }
                        )
                    }
                }
            }
        }
    }
}
