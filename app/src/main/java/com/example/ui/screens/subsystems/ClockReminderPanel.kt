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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReminderItem
import com.example.service.HapticSoundManager
import com.example.ui.components.FridayHudButton
import com.example.ui.components.HudSteppedProgressBar
import com.example.ui.theme.LocalFridayColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ClockReminderPanel(
    reminders: List<ReminderItem>,
    onSaveReminder: (ReminderItem) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onClosePanel: () -> Unit,
    hapticSoundManager: HapticSoundManager,
    soundEnabled: Boolean,
    hapticEnabled: Boolean
) {
    val hudColors = LocalFridayColors.current

    // Live Clock State
    var currentTimeFormatted by remember { mutableStateOf("") }
    var currentDateFormatted by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(now)
            currentDateFormatted = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(now).uppercase(Locale.US)
            delay(1000)
        }
    }

    // Countdown Timer State
    var targetTimerSeconds by remember { mutableLongStateOf(300L) } // default 5 minutes
    var remainingTimerSeconds by remember { mutableLongStateOf(300L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, remainingTimerSeconds) {
        if (isTimerRunning && remainingTimerSeconds > 0) {
            delay(1000)
            remainingTimerSeconds -= 1
            if (remainingTimerSeconds <= 0L) {
                isTimerRunning = false
                hapticSoundManager.playConfirm(soundEnabled, hapticEnabled)
            }
        }
    }

    // New reminder dialog state
    var showReminderDialog by remember { mutableStateOf(false) }
    var reminderTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }

    val timerMinutes = remainingTimerSeconds / 60
    val timerSecs = remainingTimerSeconds % 60
    val timerFormatted = String.format(Locale.US, "%02d:%02d", timerMinutes, timerSecs)
    val timerPercent = if (targetTimerSeconds > 0) {
        ((remainingTimerSeconds.toFloat() / targetTimerSeconds.toFloat()) * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clock_panel")
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
                    text = "CHRONO TELEMETRY & TACTICAL TIMERS",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$currentDateFormatted // $currentTimeFormatted",
                    color = hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            FridayHudButton(
                text = "RETURN",
                testTag = "btn_return_clock",
                onClick = onClosePanel,
                modifier = Modifier.width(80.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Box: Countdown Timer & Digital Chrono
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.panelBg)
                    .border(1.dp, hudColors.border.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COUNTDOWN TIMER",
                    color = hudColors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Big digital display
                Text(
                    text = timerFormatted,
                    color = if (remainingTimerSeconds <= 10L && isTimerRunning) hudColors.warning else hudColors.textPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                HudSteppedProgressBar(
                    progressPercent = timerPercent,
                    totalBars = 20,
                    fillColor = hudColors.accent,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(60L to "1M", 300L to "5M", 600L to "10M", 1500L to "25M").forEach { (sec, label) ->
                        FridayHudButton(
                            text = label,
                            onClick = {
                                isTimerRunning = false
                                targetTimerSeconds = sec
                                remainingTimerSeconds = sec
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FridayHudButton(
                        text = if (isTimerRunning) "PAUSE" else "START",
                        isActive = isTimerRunning,
                        onClick = {
                            isTimerRunning = !isTimerRunning
                            hapticSoundManager.playClick(soundEnabled, hapticEnabled)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FridayHudButton(
                        text = "RESET",
                        isWarning = true,
                        onClick = {
                            isTimerRunning = false
                            remainingTimerSeconds = targetTimerSeconds
                            hapticSoundManager.playClick(soundEnabled, hapticEnabled)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Right Box: Reminders List
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.panelBg)
                    .border(1.dp, hudColors.border.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE ALERTS & REMINDERS",
                        color = hudColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    FridayHudButton(
                        text = "+ ALERT",
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.width(75.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (reminders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO ACTIVE REMINDERS SCHEDULED.",
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
                        items(reminders, key = { it.id }) { rem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(hudColors.border.copy(alpha = 0.08f))
                                    .border(1.dp, hudColors.border.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rem.title,
                                        color = hudColors.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = rem.timeFormatted,
                                        color = hudColors.accent,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                FridayHudButton(
                                    text = "DEL",
                                    isWarning = true,
                                    onClick = { onDeleteReminder(rem.id) },
                                    modifier = Modifier.width(46.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add reminder modal
    if (showReminderDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hudColors.background.copy(alpha = 0.85f))
                .clickable { showReminderDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(380.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(hudColors.panelBg)
                    .border(1.5.dp, hudColors.primary, RoundedCornerShape(6.dp))
                    .clickable(enabled = false) {}
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SCHEDULE TACTICAL REMINDER",
                    color = hudColors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = reminderTitle,
                    onValueChange = { reminderTitle = it.uppercase() },
                    label = { Text("REMINDER LABEL", color = hudColors.textMuted, fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = hudColors.textPrimary,
                        unfocusedTextColor = hudColors.textPrimary,
                        focusedBorderColor = hudColors.primary,
                        unfocusedBorderColor = hudColors.border
                    )
                )

                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it.uppercase() },
                    label = { Text("TIME (E.G. 18:00 / IN 30 MIN)", color = hudColors.textMuted, fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = hudColors.textPrimary,
                        unfocusedTextColor = hudColors.textPrimary,
                        focusedBorderColor = hudColors.primary,
                        unfocusedBorderColor = hudColors.border
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FridayHudButton(
                        text = "CANCEL",
                        onClick = { showReminderDialog = false },
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FridayHudButton(
                        text = "SCHEDULE",
                        isActive = true,
                        enabled = reminderTitle.isNotBlank(),
                        onClick = {
                            val item = ReminderItem(
                                id = UUID.randomUUID().toString(),
                                title = reminderTitle.trim(),
                                timeFormatted = reminderTime.ifBlank { "ACTIVE REMINDER" },
                                isEnabled = true
                            )
                            onSaveReminder(item)
                            showReminderDialog = false
                            reminderTitle = ""
                            reminderTime = ""
                        },
                        modifier = Modifier.width(110.dp)
                    )
                }
            }
        }
    }
}
