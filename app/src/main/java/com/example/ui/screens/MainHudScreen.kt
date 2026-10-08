package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FridayRepository
import com.example.model.AnimationIntensity
import com.example.model.AudioRecordItem
import com.example.model.FridaySettings
import com.example.model.HardwareTelemetry
import com.example.model.MainOption
import com.example.model.NoteItem
import com.example.model.ProtocolItem
import com.example.model.ReminderItem
import com.example.model.TodoItem
import com.example.service.FridayAiService
import com.example.service.HapticSoundManager
import com.example.service.SystemAnalysisManager
import com.example.service.VoiceRecorderManager
import com.example.ui.components.FridayHudButton
import com.example.ui.components.FridayHudPanel
import com.example.ui.components.HudSteppedProgressBar
import com.example.ui.components.HudTelemetryRow
import com.example.ui.components.ScanlineAndGridBackground
import com.example.ui.screens.subsystems.AiTerminalPanel
import com.example.ui.screens.subsystems.BlackBoardPanel
import com.example.ui.screens.subsystems.ClockReminderPanel
import com.example.ui.screens.subsystems.NotesPanel
import com.example.ui.screens.subsystems.ProtocolPanel
import com.example.ui.screens.subsystems.SettingsPanel
import com.example.ui.screens.subsystems.SystemResetDialog
import com.example.ui.screens.subsystems.TodoPanel
import com.example.ui.screens.subsystems.VoiceRecorderPanel
import com.example.ui.theme.LocalFridayColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainHudScreen(
    repository: FridayRepository,
    systemAnalysisManager: SystemAnalysisManager,
    voiceRecorderManager: VoiceRecorderManager,
    aiService: FridayAiService,
    hapticSoundManager: HapticSoundManager,
    settings: FridaySettings,
    onSettingsChanged: (FridaySettings) -> Unit,
    onRequestChangePin: () -> Unit,
    onSecurityReset: () -> Unit
) {
    val context = LocalContext.current
    val hudColors = LocalFridayColors.current

    // Live Telemetry
    var telemetry by remember { mutableStateOf(systemAnalysisManager.readCurrentTelemetry()) }
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    // Subsystem Data States
    var protocols by remember { mutableStateOf(repository.getProtocols()) }
    var todos by remember { mutableStateOf(repository.getTodos()) }
    var notes by remember { mutableStateOf(repository.getNotes()) }
    var reminders by remember { mutableStateOf(repository.getReminders()) }
    var recordings by remember { mutableStateOf(repository.getRecordings()) }

    // Active Feature Navigation State
    var activeOption by remember { mutableStateOf<MainOption?>(null) }
    var showAiTerminal by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var hudFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Periodic live updates
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(now)
            currentDateStr = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(now).uppercase(Locale.US)
            telemetry = systemAnalysisManager.readCurrentTelemetry()
            delay(1000)
        }
    }

    // Clear feedback message after delay
    LaunchedEffect(hudFeedbackMessage) {
        if (hudFeedbackMessage != null) {
            delay(4000)
            hudFeedbackMessage = null
        }
    }

    // Android Back Button handler
    BackHandler(enabled = activeOption != null || showAiTerminal || showResetDialog) {
        if (showResetDialog) {
            showResetDialog = false
        } else if (showAiTerminal) {
            showAiTerminal = false
        } else if (activeOption != null) {
            activeOption = null
        }
    }

    fun handleProtocolExecution(protocol: ProtocolItem) {
        hapticSoundManager.playConfirm(settings.soundEffectsEnabled, settings.hapticFeedbackEnabled)
        when (protocol.actionType) {
            "CLOSE_APP" -> {
                (context as? Activity)?.finishAffinity()
            }
            "SYSTEM_INFO" -> {
                activeOption = null
                hudFeedbackMessage = "PROTOCOL ${protocol.code} EXECUTED: SYSTEM TELEMETRY AUDIT COMPLETE"
            }
            "LAUNCH_NOTES" -> activeOption = MainOption.NOTES
            "LAUNCH_TODO" -> activeOption = MainOption.TODO
            "LAUNCH_RECORDER" -> activeOption = MainOption.VOICE_RECORDER
            "LAUNCH_TIMER" -> activeOption = MainOption.CLOCK_REMINDER
            "LAUNCH_BOARD" -> activeOption = MainOption.BLACK_BOARD
            else -> {
                hudFeedbackMessage = "PROTOCOL ${protocol.code} ENGAGED: ${protocol.description}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(hudColors.background)
            .testTag("main_hud_screen")
    ) {
        ScanlineAndGridBackground(intensity = settings.animationIntensity)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Global HUD Top Bar (Sleek sci-fi header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(hudColors.panelBg)
                    .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FRIDAY SYSTEM",
                        color = hudColors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "// MK-85 TACTICAL HUD",
                        color = hudColors.accent,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (hudFeedbackMessage != null) {
                    Text(
                        text = hudFeedbackMessage!!,
                        color = hudColors.success,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "$currentDateStr // $currentTimeStr",
                        color = hudColors.textSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // AI Status Badge & Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (aiService.isOnlineAiAvailable()) hudColors.success.copy(alpha = 0.2f)
                                else hudColors.primary.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (aiService.isOnlineAiAvailable()) hudColors.success else hudColors.primary,
                                RoundedCornerShape(2.dp)
                            )
                            .clickable {
                                hapticSoundManager.playClick(settings.soundEffectsEnabled, settings.hapticFeedbackEnabled)
                                showAiTerminal = !showAiTerminal
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aiService.getAiStatus(),
                            color = if (aiService.isOnlineAiAvailable()) hudColors.success else hudColors.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // System condition indicator
                    Text(
                        text = "SYS: ${telemetry.systemCondition}",
                        color = if (telemetry.systemCondition == "OPTIMAL") hudColors.success else hudColors.warning,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Three Visual Areas (Landscape Layout)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // AREA 1: LEFT SIDE — FULL SYSTEM ANALYSIS
                FridayHudPanel(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight(),
                    title = "SYSTEM ANALYSIS",
                    statusText = telemetry.systemCondition
                ) {
                    LeftSystemAnalysisBoard(telemetry = telemetry)
                }

                // AREA 2: CENTER — FRIDAY SYSTEM STATUS & ACTIVE SUBSYSTEM
                FridayHudPanel(
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight(),
                    title = when {
                        showAiTerminal -> "GEMINI AI TACTICAL TERMINAL"
                        activeOption != null -> activeOption!!.name.replace("_", " ")
                        else -> "FRIDAY SYSTEM STATUS"
                    },
                    statusText = if (activeOption != null || showAiTerminal) "ENGAGED" else "MONITORING"
                ) {
                    AnimatedContent(
                        targetState = Pair(activeOption, showAiTerminal),
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                        label = "center_content_switch"
                    ) { (option, isAi) ->
                        when {
                            isAi -> {
                                AiTerminalPanel(
                                    aiService = aiService,
                                    systemContextSummary = "BATTERY: ${telemetry.batteryPercent}%, RAM AVAIL: ${telemetry.ramAvailableMb}MB, CONDITION: ${telemetry.systemCondition}",
                                    onClosePanel = { showAiTerminal = false }
                                )
                            }
                            option == MainOption.PROTOCOL -> {
                                ProtocolPanel(
                                    protocols = protocols,
                                    onExecuteProtocol = { handleProtocolExecution(it) },
                                    onSaveProtocol = {
                                        val updated = protocols + it
                                        protocols = updated
                                        repository.saveProtocols(updated)
                                    },
                                    onDeleteProtocol = { id ->
                                        val updated = protocols.filter { it.id != id }
                                        protocols = updated
                                        repository.saveProtocols(updated)
                                    },
                                    onClosePanel = { activeOption = null }
                                )
                            }
                            option == MainOption.TODO -> {
                                TodoPanel(
                                    todos = todos,
                                    onSaveTodo = {
                                        val updated = todos + it
                                        todos = updated
                                        repository.saveTodos(updated)
                                    },
                                    onToggleTodo = { id ->
                                        val updated = todos.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
                                        todos = updated
                                        repository.saveTodos(updated)
                                    },
                                    onDeleteTodo = { id ->
                                        val updated = todos.filter { it.id != id }
                                        todos = updated
                                        repository.saveTodos(updated)
                                    },
                                    onClosePanel = { activeOption = null }
                                )
                            }
                            option == MainOption.BLACK_BOARD -> {
                                BlackBoardPanel(
                                    onClosePanel = { activeOption = null }
                                )
                            }
                            option == MainOption.NOTES -> {
                                NotesPanel(
                                    notes = notes,
                                    onSaveNote = { note ->
                                        val existing = notes.indexOfFirst { it.id == note.id }
                                        val updated = if (existing >= 0) {
                                            notes.toMutableList().apply { set(existing, note) }
                                        } else {
                                            notes + note
                                        }
                                        notes = updated
                                        repository.saveNotes(updated)
                                    },
                                    onDeleteNote = { id ->
                                        val updated = notes.filter { it.id != id }
                                        notes = updated
                                        repository.saveNotes(updated)
                                    },
                                    onTogglePin = { id ->
                                        val updated = notes.map { if (it.id == id) it.copy(isPinned = !it.isPinned) else it }
                                        notes = updated
                                        repository.saveNotes(updated)
                                    },
                                    onClosePanel = { activeOption = null }
                                )
                            }
                            option == MainOption.SETTINGS -> {
                                SettingsPanel(
                                    settings = settings,
                                    onSaveSettings = {
                                        repository.saveSettings(it)
                                        onSettingsChanged(it)
                                    },
                                    onRequestChangePin = onRequestChangePin,
                                    onClosePanel = { activeOption = null }
                                )
                            }
                            option == MainOption.CLOCK_REMINDER -> {
                                ClockReminderPanel(
                                    reminders = reminders,
                                    onSaveReminder = {
                                        val updated = reminders + it
                                        reminders = updated
                                        repository.saveReminders(updated)
                                    },
                                    onDeleteReminder = { id ->
                                        val updated = reminders.filter { it.id != id }
                                        reminders = updated
                                        repository.saveReminders(updated)
                                    },
                                    onClosePanel = { activeOption = null },
                                    hapticSoundManager = hapticSoundManager,
                                    soundEnabled = settings.soundEffectsEnabled,
                                    hapticEnabled = settings.hapticFeedbackEnabled
                                )
                            }
                            option == MainOption.VOICE_RECORDER -> {
                                VoiceRecorderPanel(
                                    recordings = recordings,
                                    recorderManager = voiceRecorderManager,
                                    onRecordingSaved = { item ->
                                        val updated = recordings + item
                                        recordings = updated
                                        repository.saveRecordings(updated)
                                    },
                                    onRecordingDeleted = { id ->
                                        val updated = recordings.filter { it.id != id }
                                        recordings = updated
                                        repository.saveRecordings(updated)
                                    },
                                    onClosePanel = { activeOption = null },
                                    hapticSoundManager = hapticSoundManager,
                                    soundEnabled = settings.soundEffectsEnabled,
                                    hapticEnabled = settings.hapticFeedbackEnabled
                                )
                            }
                            else -> {
                                // Default Center Holographic Display (HUD Data Visualizations)
                                CenterHolographicHub(
                                    telemetry = telemetry,
                                    currentTime = currentTimeStr,
                                    currentDate = currentDateStr,
                                    aiStatus = aiService.getAiStatus(),
                                    onOpenAiTerminal = { showAiTerminal = true },
                                    intensity = settings.animationIntensity
                                )
                            }
                        }
                    }
                }

                // AREA 3: RIGHT SIDE — 8 SYSTEM OPTIONS
                FridayHudPanel(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight(),
                    title = "SYSTEM OPTIONS (8)",
                    statusText = "COMMAND MATRIX"
                ) {
                    RightEightOptionsGrid(
                        activeOption = activeOption,
                        onOptionSelected = { opt ->
                            hapticSoundManager.playClick(settings.soundEffectsEnabled, settings.hapticFeedbackEnabled)
                            showAiTerminal = false
                            if (opt == MainOption.SYSTEM_RESET) {
                                showResetDialog = true
                            } else {
                                activeOption = if (activeOption == opt) null else opt
                            }
                        }
                    )
                }
            }
        }

        // System Reset Warning Dialog Overlay
        if (showResetDialog) {
            SystemResetDialog(
                onConfirmReset = { rNotes, rTodos, rProtocols, rSettings, rPin ->
                    repository.resetData(rNotes, rTodos, rProtocols, rSettings, rPin)
                    if (rNotes) notes = emptyList()
                    if (rTodos) todos = emptyList()
                    if (rProtocols) protocols = repository.getProtocols()
                    if (rSettings) onSettingsChanged(repository.getSettings())
                    showResetDialog = false
                    hudFeedbackMessage = "SYSTEM DATA RESET COMPLETE"
                    hapticSoundManager.playConfirm(settings.soundEffectsEnabled, settings.hapticFeedbackEnabled)
                    if (rPin) {
                        onSecurityReset()
                    }
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}

// ----------------- LEFT PANEL: FULL SYSTEM ANALYSIS -----------------
@Composable
private fun LeftSystemAnalysisBoard(telemetry: HardwareTelemetry) {
    val hudColors = LocalFridayColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            // Power Subsystem
            Text(
                text = "POWER SUBSYSTEM",
                color = hudColors.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            HudTelemetryRow("BATTERY LEVEL", "${telemetry.batteryPercent}%", accentValue = true)
            HudSteppedProgressBar(
                progressPercent = telemetry.batteryPercent,
                totalBars = 10,
                fillColor = if (telemetry.batteryPercent < 20) hudColors.warning else hudColors.primary
            )
            HudTelemetryRow("BATTERY TEMP", "${String.format(Locale.US, "%.1f", telemetry.batteryTemperatureC)} °C")
            HudTelemetryRow("CHARGING STATUS", if (telemetry.isCharging) "CHARGING [AC/USB]" else "DISCHARGING")
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            // Memory & Storage
            Text(
                text = "MEMORY & STORAGE",
                color = hudColors.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            HudTelemetryRow("RAM TOTAL", "${telemetry.ramTotalMb} MB")
            HudTelemetryRow("RAM AVAILABLE", "${telemetry.ramAvailableMb} MB")
            val ramPercent = if (telemetry.ramTotalMb > 0) {
                (((telemetry.ramTotalMb - telemetry.ramAvailableMb).toFloat() / telemetry.ramTotalMb.toFloat()) * 100).toInt()
            } else 50
            HudSteppedProgressBar(progressPercent = ramPercent, totalBars = 10, fillColor = hudColors.accent)
            HudTelemetryRow("APP MEM USAGE", "${telemetry.appMemoryMb} MB")
            HudTelemetryRow("STORAGE TOTAL", "${String.format(Locale.US, "%.1f", telemetry.storageTotalGb)} GB")
            HudTelemetryRow("STORAGE USED", "${String.format(Locale.US, "%.1f", telemetry.storageUsedGb)} GB")
            HudTelemetryRow("STORAGE FREE", "${String.format(Locale.US, "%.1f", telemetry.storageAvailableGb)} GB")
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            // Device Core & Hardware
            Text(
                text = "DEVICE CORE",
                color = hudColors.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            HudTelemetryRow("MANUFACTURER", telemetry.manufacturer)
            HudTelemetryRow("MODEL", telemetry.model)
            HudTelemetryRow("ANDROID OS", "V${telemetry.androidVersion}")
            HudTelemetryRow("API LEVEL", "${telemetry.apiLevel}")
            HudTelemetryRow("CPU ARCH", telemetry.cpuArch)
            HudTelemetryRow("CPU CORES", "${telemetry.cpuCores} LOGICAL CORES")
            HudTelemetryRow("DEVICE BITS", if (telemetry.is64Bit) "64-BIT ARCH" else "32-BIT ARCH")
            HudTelemetryRow("RESOLUTION", telemetry.screenResolution)
            HudTelemetryRow("REFRESH RATE", "${telemetry.refreshRateHz.toInt()} HZ")
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            // Telemetry & Links
            Text(
                text = "CONNECTIVITY & UPTIME",
                color = hudColors.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            HudTelemetryRow("NETWORK LINK", telemetry.networkType)
            HudTelemetryRow("WI-FI LINK", if (telemetry.isWifiConnected) "ONLINE" else "DISCONNECTED")
            HudTelemetryRow("BLUETOOTH", if (telemetry.isBluetoothEnabled) "ACTIVE" else "STANDBY")
            HudTelemetryRow("DEVICE UPTIME", telemetry.uptimeFormatted)
            HudTelemetryRow(
                "SYSTEM CONDITION",
                "SYSTEM CONDITION: ${telemetry.systemCondition}",
                valueColor = if (telemetry.systemCondition == "OPTIMAL") hudColors.success else hudColors.warning
            )
        }
    }
}

// ----------------- CENTER PANEL: DEFAULT HOLOGRAPHIC HUB -----------------
@Composable
private fun CenterHolographicHub(
    telemetry: HardwareTelemetry,
    currentTime: String,
    currentDate: String,
    aiStatus: String,
    onOpenAiTerminal: () -> Unit,
    intensity: AnimationIntensity
) {
    val hudColors = LocalFridayColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "hud_rot")

    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (intensity == AnimationIntensity.HIGH) 4000 else 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rot"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Holographic Telemetry Rings Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = minOf(size.width, size.height) * 0.45f

                // Outer telemetry ring
                drawCircle(
                    color = hudColors.primary.copy(alpha = 0.25f),
                    radius = maxRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Middle dashed ring
                drawCircle(
                    color = hudColors.border.copy(alpha = 0.2f),
                    radius = maxRadius * 0.75f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Inner core ring
                drawCircle(
                    color = hudColors.accent.copy(alpha = 0.35f),
                    radius = maxRadius * 0.45f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Rotating radar sweep line (subtle scan effect)
                if (intensity != AnimationIntensity.LOW) {
                    val rad = Math.toRadians(radarAngle.toDouble())
                    val endX = center.x + (maxRadius * Math.cos(rad)).toFloat()
                    val endY = center.y + (maxRadius * Math.sin(rad)).toFloat()
                    drawLine(
                        color = hudColors.primary.copy(alpha = 0.5f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // Central Sci-Fi Identity & Clock
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FRIDAY CORE",
                    color = hudColors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Text(
                    text = currentTime,
                    color = hudColors.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Text(
                    text = currentDate,
                    color = hudColors.accent,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Bottom Telemetry Strip in Center Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(hudColors.border.copy(alpha = 0.08f))
                .border(1.dp, hudColors.border.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI COGNITIVE SUBSYSTEM",
                    color = hudColors.textMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = aiStatus,
                    color = hudColors.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            FridayHudButton(
                text = "OPEN AI TERMINAL",
                testTag = "btn_open_ai_terminal",
                onClick = onOpenAiTerminal,
                modifier = Modifier.width(135.dp)
            )
        }
    }
}

// ----------------- RIGHT PANEL: EXACTLY 8 SYSTEM OPTIONS -----------------
@Composable
private fun RightEightOptionsGrid(
    activeOption: MainOption?,
    onOptionSelected: (MainOption) -> Unit
) {
    val hudColors = LocalFridayColors.current

    // Exactly 8 System Options:
    // 1. PROTOCOL
    // 2. TO DO
    // 3. BLACK BOARD
    // 4. NOTES
    // 5. SYSTEM RESET
    // 6. SETTINGS
    // 7. CLOCK / REMINDER
    // 8. VOICE RECORDER
    val optionsList = listOf(
        OptionDefinition(MainOption.PROTOCOL, "1. PROTOCOL", "[SYS]", false),
        OptionDefinition(MainOption.TODO, "2. TO DO", "[TASK]", false),
        OptionDefinition(MainOption.BLACK_BOARD, "3. BLACK BOARD", "[DRAW]", false),
        OptionDefinition(MainOption.NOTES, "4. NOTES", "[MEMO]", false),
        OptionDefinition(MainOption.CLOCK_REMINDER, "7. CLOCK / ALERTS", "[TIME]", false),
        OptionDefinition(MainOption.VOICE_RECORDER, "8. VOICE RECORDER", "[AUDIO]", false),
        OptionDefinition(MainOption.SETTINGS, "6. SETTINGS", "[CONF]", false),
        OptionDefinition(MainOption.SYSTEM_RESET, "5. SYSTEM RESET", "[WARN]", true)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        optionsList.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowPair.forEach { def ->
                    val isSelected = activeOption == def.option
                    HudOptionButton(
                        definition = def,
                        isSelected = isSelected,
                        onClick = { onOptionSelected(def.option) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

private data class OptionDefinition(
    val option: MainOption,
    val title: String,
    val iconTag: String,
    val isWarning: Boolean
)

@Composable
private fun HudOptionButton(
    definition: OptionDefinition,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudColors = LocalFridayColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val accentColor = when {
        definition.isWarning -> hudColors.warning
        isSelected -> hudColors.accent
        else -> hudColors.primary
    }

    val bgAlpha = when {
        isPressed -> 0.4f
        isSelected -> 0.28f
        else -> 0.1f
    }

    Box(
        modifier = modifier
            .testTag("option_${definition.option.name}")
            .clip(RoundedCornerShape(4.dp))
            .background(accentColor.copy(alpha = bgAlpha))
            .border(
                width = if (isSelected || isPressed) 1.5.dp else 1.dp,
                color = accentColor.copy(alpha = if (isSelected || isPressed) 0.9f else 0.45f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = definition.title.uppercase(),
                    color = if (isSelected) hudColors.textPrimary else accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = definition.iconTag,
                color = accentColor.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
