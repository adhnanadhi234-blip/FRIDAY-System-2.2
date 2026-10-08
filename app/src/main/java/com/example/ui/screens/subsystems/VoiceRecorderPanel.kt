package com.example.ui.screens.subsystems

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AudioRecordItem
import com.example.service.HapticSoundManager
import com.example.service.VoiceRecorderManager
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun VoiceRecorderPanel(
    recordings: List<AudioRecordItem>,
    recorderManager: VoiceRecorderManager,
    onRecordingSaved: (AudioRecordItem) -> Unit,
    onRecordingDeleted: (String) -> Unit,
    onClosePanel: () -> Unit,
    hapticSoundManager: HapticSoundManager,
    soundEnabled: Boolean,
    hapticEnabled: Boolean
) {
    val context = LocalContext.current
    val hudColors = LocalFridayColors.current

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordDurationSeconds by remember { mutableLongStateOf(0L) }
    var permissionError by remember { mutableStateOf<String?>(null) }
    var playingFilePath by remember { mutableStateOf<String?>(null) }

    // Waveform simulation bars based on live amplitude
    val waveformBars = remember { mutableStateListOf(10, 20, 15, 30, 25, 40, 35, 20, 15, 10) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionError = null
            val started = recorderManager.startRecording()
            if (started) {
                isRecording = true
                isPaused = false
                recordDurationSeconds = 0L
                hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
            } else {
                permissionError = "HARDWARE IO ERROR: MICROPHONE INACCESSIBLE"
            }
        } else {
            permissionError = "SECURITY PROTOCOL VIOLATION: MICROPHONE PERMISSION DENIED"
            hapticSoundManager.playError(soundEnabled, hapticEnabled)
        }
    }

    // Timer & Amplitude sampler
    LaunchedEffect(isRecording, isPaused) {
        while (isRecording) {
            delay(100)
            if (!isPaused) {
                val amp = recorderManager.getMaxAmplitude()
                val normalized = (amp / 800).coerceIn(4, 50)
                if (waveformBars.isNotEmpty()) {
                    waveformBars.removeAt(0)
                    waveformBars.add(normalized)
                }
            }
        }
    }

    LaunchedEffect(isRecording, isPaused) {
        while (isRecording) {
            delay(1000)
            if (!isPaused) {
                recordDurationSeconds += 1
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (recorderManager.isCurrentlyRecording()) {
                recorderManager.stopRecording(recordDurationSeconds)
            }
            recorderManager.stopPlayback()
        }
    }

    val recMinutes = recordDurationSeconds / 60
    val recSecs = recordDurationSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", recMinutes, recSecs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("voice_recorder_panel")
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
                    text = "ACOUSTIC SURVEILLANCE & VOICE LOG",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isRecording) "STATUS: RECORDING AUDIO [ACTIVE]" else "STATUS: STANDBY",
                    color = if (isRecording) hudColors.warning else hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            FridayHudButton(
                text = "RETURN",
                testTag = "btn_return_recorder",
                onClick = onClosePanel,
                modifier = Modifier.width(80.dp)
            )
        }

        if (permissionError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.warning.copy(alpha = 0.2f))
                    .border(1.dp, hudColors.warning, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = permissionError!!,
                    color = hudColors.warning,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Box: Live Recording Deck
            Column(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.panelBg)
                    .border(1.dp, hudColors.border.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TACTICAL AUDIO INPUT",
                    color = hudColors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Big Timer & Record Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (isRecording && !isPaused) hudColors.warning else Color.Gray.copy(alpha = 0.3f))
                    )
                    Text(
                        text = timeFormatted,
                        color = if (isRecording) hudColors.textPrimary else hudColors.textMuted,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Waveform Bars Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF03070E))
                        .border(1.dp, hudColors.border.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    waveformBars.forEach { barHeight ->
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(if (isRecording && !isPaused) barHeight.dp else 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isRecording) hudColors.accent else hudColors.border.copy(alpha = 0.2f))
                        )
                    }
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isRecording) {
                        FridayHudButton(
                            text = "START RECORDING",
                            testTag = "btn_start_record",
                            isActive = true,
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    val ok = recorderManager.startRecording()
                                    if (ok) {
                                        isRecording = true
                                        isPaused = false
                                        recordDurationSeconds = 0L
                                        hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        FridayHudButton(
                            text = if (isPaused) "RESUME" else "PAUSE",
                            onClick = {
                                if (isPaused) {
                                    recorderManager.resumeRecording()
                                    isPaused = false
                                } else {
                                    recorderManager.pauseRecording()
                                    isPaused = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FridayHudButton(
                            text = "STOP & SAVE",
                            isWarning = true,
                            onClick = {
                                val savedItem = recorderManager.stopRecording(recordDurationSeconds)
                                isRecording = false
                                isPaused = false
                                if (savedItem != null) {
                                    onRecordingSaved(savedItem)
                                    hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Right Box: Saved Audio Logs
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.panelBg)
                    .border(1.dp, hudColors.border.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "RECORDED AUDIO TRANSMISSIONS (${recordings.size})",
                    color = hudColors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (recordings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO ENCRYPTED AUDIO FILES ON DISK.",
                            color = hudColors.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(recordings, key = { it.id }) { item ->
                            val isPlayingThis = playingFilePath == item.filePath
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(hudColors.border.copy(alpha = 0.08f))
                                    .border(1.dp, if (isPlayingThis) hudColors.accent else hudColors.border.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.fileName,
                                        color = hudColors.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.durationSeconds} SECONDS",
                                        color = hudColors.accent,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FridayHudButton(
                                        text = if (isPlayingThis) "STOP" else "PLAY",
                                        isActive = isPlayingThis,
                                        onClick = {
                                            if (isPlayingThis) {
                                                recorderManager.stopPlayback()
                                                playingFilePath = null
                                            } else {
                                                val started = recorderManager.playRecording(
                                                    filePath = item.filePath,
                                                    onCompletion = { playingFilePath = null },
                                                    onError = { playingFilePath = null }
                                                )
                                                if (started) playingFilePath = item.filePath
                                            }
                                        },
                                        modifier = Modifier.width(55.dp)
                                    )
                                    FridayHudButton(
                                        text = "DEL",
                                        isWarning = true,
                                        onClick = {
                                            recorderManager.deleteRecording(item.filePath)
                                            onRecordingDeleted(item.id)
                                        },
                                        modifier = Modifier.width(45.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
