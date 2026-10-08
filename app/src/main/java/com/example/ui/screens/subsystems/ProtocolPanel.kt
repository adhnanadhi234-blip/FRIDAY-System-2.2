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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MainOption
import com.example.model.ProtocolItem
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors
import java.util.UUID

@Composable
fun ProtocolPanel(
    protocols: List<ProtocolItem>,
    onExecuteProtocol: (ProtocolItem) -> Unit,
    onSaveProtocol: (ProtocolItem) -> Unit,
    onDeleteProtocol: (String) -> Unit,
    onClosePanel: () -> Unit
) {
    val hudColors = LocalFridayColors.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var newCode by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newActionType by remember { mutableStateOf("CUSTOM_ALERT") }
    var newCustomMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("protocol_panel")
    ) {
        // Subsystem Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PROTOCOL EXECUTION ENGINE",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "ACTIVE PROTOCOLS: ${protocols.size}",
                    color = hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FridayHudButton(
                    text = "+ NEW PROTOCOL",
                    testTag = "btn_new_protocol",
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.width(130.dp)
                )
                FridayHudButton(
                    text = "RETURN",
                    testTag = "btn_return_protocol",
                    onClick = onClosePanel,
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        if (activeMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(hudColors.primary.copy(alpha = 0.2f))
                    .border(1.dp, hudColors.primary, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = activeMessage!!,
                    color = hudColors.textPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Protocol List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(protocols, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(hudColors.panelBg)
                        .border(1.dp, hudColors.border.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${item.code}]",
                                color = hudColors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                color = hudColors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = item.description,
                            color = hudColors.textSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FridayHudButton(
                            text = "EXECUTE",
                            testTag = "exec_${item.code}",
                            isActive = true,
                            onClick = {
                                activeMessage = "PROTOCOL [${item.code}] ENGAGED: ${item.title}"
                                onExecuteProtocol(item)
                            },
                            modifier = Modifier.width(90.dp)
                        )
                        if (!item.isSystemPreset) {
                            FridayHudButton(
                                text = "DEL",
                                testTag = "del_${item.code}",
                                isWarning = true,
                                onClick = { onDeleteProtocol(item.id) },
                                modifier = Modifier.width(50.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Protocol Modal Dialog
    if (showCreateDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hudColors.background.copy(alpha = 0.85f))
                .clickable { showCreateDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(hudColors.panelBg)
                    .border(1.5.dp, hudColors.primary, RoundedCornerShape(6.dp))
                    .clickable(enabled = false) {}
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DEFINE CUSTOM PROTOCOL",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = newCode,
                    onValueChange = { newCode = it.uppercase() },
                    label = { Text("PROTOCOL CODE (E.G., T450)", color = hudColors.textMuted, fontSize = 10.sp) },
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
                    value = newTitle,
                    onValueChange = { newTitle = it.uppercase() },
                    label = { Text("PROTOCOL TITLE", color = hudColors.textMuted, fontSize = 10.sp) },
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
                    value = newDescription,
                    onValueChange = { newDescription = it.uppercase() },
                    label = { Text("ACTION DESCRIPTION", color = hudColors.textMuted, fontSize = 10.sp) },
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
                        onClick = { showCreateDialog = false },
                        modifier = Modifier.width(90.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FridayHudButton(
                        text = "SAVE PROTOCOL",
                        isActive = true,
                        enabled = newCode.isNotBlank() && newTitle.isNotBlank(),
                        onClick = {
                            val newItem = ProtocolItem(
                                id = UUID.randomUUID().toString(),
                                code = newCode.trim(),
                                title = newTitle.trim(),
                                actionType = newActionType,
                                description = newDescription.ifBlank { "CUSTOM TACTICAL DIRECTIVE" },
                                customMessage = newCustomMsg,
                                isSystemPreset = false
                            )
                            onSaveProtocol(newItem)
                            showCreateDialog = false
                            newCode = ""
                            newTitle = ""
                            newDescription = ""
                        },
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
    }
}
