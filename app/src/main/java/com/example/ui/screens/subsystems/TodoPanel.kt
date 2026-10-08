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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PriorityLevel
import com.example.model.TodoItem
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun TodoPanel(
    todos: List<TodoItem>,
    onSaveTodo: (TodoItem) -> Unit,
    onToggleTodo: (String) -> Unit,
    onDeleteTodo: (String) -> Unit,
    onClosePanel: () -> Unit
) {
    val hudColors = LocalFridayColors.current
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newPriority by remember { mutableStateOf(PriorityLevel.NORMAL) }

    val completedCount = todos.count { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("todo_panel")
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
                    text = "TACTICAL MISSION DIRECTIVES // TO-DO",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PROGRESS: $completedCount / ${todos.size} DIRECTIVES COMPLETED",
                    color = hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FridayHudButton(
                    text = "+ NEW TASK",
                    testTag = "btn_new_task",
                    onClick = { showAddDialog = true },
                    modifier = Modifier.width(110.dp)
                )
                FridayHudButton(
                    text = "RETURN",
                    testTag = "btn_return_todo",
                    onClick = onClosePanel,
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        // List
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO ACTIVE DIRECTIVES. TAP '+ NEW TASK' TO LOG A DIRECTIVE.",
                    color = hudColors.textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(todos, key = { it.id }) { item ->
                    val priorityColor = when (item.priority) {
                        PriorityLevel.CRITICAL -> hudColors.warning
                        PriorityLevel.HIGH -> Color(0xFFFF9800)
                        PriorityLevel.NORMAL -> hudColors.primary
                        PriorityLevel.LOW -> hudColors.textMuted
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(hudColors.panelBg)
                            .border(1.dp, if (item.isCompleted) hudColors.border.copy(alpha = 0.2f) else hudColors.border.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox status indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (item.isCompleted) hudColors.success else Color.Transparent)
                                .border(1.5.dp, if (item.isCompleted) hudColors.success else hudColors.primary, CircleShape)
                                .clickable { onToggleTodo(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isCompleted) {
                                Text("✓", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(priorityColor.copy(alpha = 0.2f))
                                        .border(1.dp, priorityColor, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = item.priority.name,
                                        color = priorityColor,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = item.title,
                                    color = if (item.isCompleted) hudColors.textMuted else hudColors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (item.description.isNotBlank()) {
                                Text(
                                    text = item.description,
                                    color = hudColors.textSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        FridayHudButton(
                            text = "DEL",
                            isWarning = true,
                            onClick = { onDeleteTodo(item.id) },
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hudColors.background.copy(alpha = 0.85f))
                .clickable { showAddDialog = false },
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
                    text = "DISPATCH NEW DIRECTIVE",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it.uppercase() },
                    label = { Text("DIRECTIVE TITLE", color = hudColors.textMuted, fontSize = 10.sp) },
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
                    value = newDesc,
                    onValueChange = { newDesc = it.uppercase() },
                    label = { Text("BRIEFING DETAILS", color = hudColors.textMuted, fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = hudColors.textPrimary,
                        unfocusedTextColor = hudColors.textPrimary,
                        focusedBorderColor = hudColors.primary,
                        unfocusedBorderColor = hudColors.border
                    )
                )

                // Priority Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRIORITY:",
                        color = hudColors.textMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    PriorityLevel.entries.forEach { p ->
                        val isSelected = newPriority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) hudColors.primary.copy(alpha = 0.3f) else Color.Transparent)
                                .border(1.dp, if (isSelected) hudColors.primary else hudColors.border.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                .clickable { newPriority = p }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.name,
                                color = if (isSelected) hudColors.textPrimary else hudColors.textSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FridayHudButton(
                        text = "CANCEL",
                        onClick = { showAddDialog = false },
                        modifier = Modifier.width(90.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FridayHudButton(
                        text = "CONFIRM TASK",
                        isActive = true,
                        enabled = newTitle.isNotBlank(),
                        onClick = {
                            val timeStr = SimpleDateFormat("HH:mm - dd MMM", Locale.US).format(Date())
                            onSaveTodo(
                                TodoItem(
                                    id = UUID.randomUUID().toString(),
                                    title = newTitle.trim(),
                                    description = newDesc.trim(),
                                    priority = newPriority,
                                    dueDate = timeStr
                                )
                            )
                            showAddDialog = false
                            newTitle = ""
                            newDesc = ""
                        },
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
    }
}
