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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors

@Composable
fun SystemResetDialog(
    onConfirmReset: (notes: Boolean, todos: Boolean, protocols: Boolean, settings: Boolean, pin: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val hudColors = LocalFridayColors.current

    var resetNotes by remember { mutableStateOf(true) }
    var resetTodos by remember { mutableStateOf(true) }
    var resetProtocols by remember { mutableStateOf(false) }
    var resetSettings by remember { mutableStateOf(false) }
    var resetSecurityPin by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .testTag("system_reset_dialog")
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(460.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(hudColors.panelBg)
                .border(2.dp, hudColors.warning, RoundedCornerShape(6.dp))
                .clickable(enabled = false) {}
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WARNING",
                    color = hudColors.warning,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "[CODE RED PROTOCOL]",
                    color = hudColors.warning,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "THIS ACTION WILL RESET FRIDAY SYSTEM DATA.",
                color = hudColors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "SELECT DATA PARTITIONS TO WIPE:",
                color = hudColors.textMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Selectable partitions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ResetCheckbox("NOTES", resetNotes) { resetNotes = !resetNotes }
                ResetCheckbox("TO-DO", resetTodos) { resetTodos = !resetTodos }
                ResetCheckbox("PROTOCOLS", resetProtocols) { resetProtocols = !resetProtocols }
                ResetCheckbox("SETTINGS", resetSettings) { resetSettings = !resetSettings }
            }

            // PIN Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.warning.copy(alpha = 0.1f))
                    .border(1.dp, hudColors.warning.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FULL SECURITY PIN PURGE",
                        color = hudColors.warning,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "REQUIRES INITIAL PIN CREATION ON NEXT BOOT",
                        color = hudColors.textMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                FridayHudButton(
                    text = if (resetSecurityPin) "PURGE PIN: YES" else "PURGE PIN: NO",
                    isWarning = resetSecurityPin,
                    onClick = { resetSecurityPin = !resetSecurityPin },
                    modifier = Modifier.width(130.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FridayHudButton(
                    text = "CANCEL",
                    onClick = onDismiss,
                    modifier = Modifier.width(90.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FridayHudButton(
                    text = "CONFIRM RESET",
                    isWarning = true,
                    onClick = {
                        onConfirmReset(resetNotes, resetTodos, resetProtocols, resetSettings, resetSecurityPin)
                    },
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}

@Composable
private fun ResetCheckbox(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val hudColors = LocalFridayColors.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (checked) hudColors.warning.copy(alpha = 0.25f) else hudColors.border.copy(alpha = 0.1f))
            .border(1.dp, if (checked) hudColors.warning else hudColors.border.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .clickable { onToggle() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${if (checked) "[✓]" else "[ ]"} $label",
            color = if (checked) hudColors.textPrimary else hudColors.textMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
