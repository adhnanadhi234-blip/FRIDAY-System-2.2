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
import com.example.model.NoteItem
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors
import java.util.UUID

@Composable
fun NotesPanel(
    notes: List<NoteItem>,
    onSaveNote: (NoteItem) -> Unit,
    onDeleteNote: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onClosePanel: () -> Unit
) {
    val hudColors = LocalFridayColors.current

    var searchQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    var formTitle by remember { mutableStateOf("") }
    var formContent by remember { mutableStateOf("") }
    var formIsPinned by remember { mutableStateOf(false) }

    val filteredNotes = notes.filter {
        val q = searchQuery.trim().uppercase()
        q.isEmpty() || it.title.uppercase().contains(q) || it.content.uppercase().contains(q)
    }.sortedByDescending { it.isPinned }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notes_panel")
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
                    text = "CLASSIFIED DATA LOGS // NOTES",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "LOGGED ENTRIES: ${notes.size} // PINNED: ${notes.count { it.isPinned }}",
                    color = hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FridayHudButton(
                    text = "+ NEW ENTRY",
                    testTag = "btn_new_note",
                    onClick = {
                        editingNote = null
                        formTitle = ""
                        formContent = ""
                        formIsPinned = false
                        isCreatingNew = true
                    },
                    modifier = Modifier.width(110.dp)
                )
                FridayHudButton(
                    text = "RETURN",
                    testTag = "btn_return_notes",
                    onClick = onClosePanel,
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.uppercase() },
            placeholder = { Text("SEARCH TACTICAL MEMOS...", color = hudColors.textMuted, fontSize = 10.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = hudColors.textPrimary,
                unfocusedTextColor = hudColors.textPrimary,
                focusedBorderColor = hudColors.primary,
                unfocusedBorderColor = hudColors.border.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Notes list
        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "NO MATCHING ENTRIES LOCATED." else "NO MEMOS LOGGED. TAP '+ NEW ENTRY' TO LOG A NOTE.",
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
                items(filteredNotes, key = { it.id }) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(hudColors.panelBg)
                            .border(
                                width = if (note.isPinned) 1.5.dp else 1.dp,
                                color = if (note.isPinned) hudColors.accent else hudColors.border.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    editingNote = note
                                    formTitle = note.title
                                    formContent = note.content
                                    formIsPinned = note.isPinned
                                    isCreatingNew = true
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (note.isPinned) {
                                    Text(
                                        text = "[★ PINNED]",
                                        color = hudColors.accent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = note.title,
                                    color = hudColors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = note.content,
                                color = hudColors.textSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FridayHudButton(
                                text = if (note.isPinned) "UNPIN" else "PIN",
                                isActive = note.isPinned,
                                onClick = { onTogglePin(note.id) },
                                modifier = Modifier.width(60.dp)
                            )
                            FridayHudButton(
                                text = "DEL",
                                isWarning = true,
                                onClick = { onDeleteNote(note.id) },
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Modal
    if (isCreatingNew) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hudColors.background.copy(alpha = 0.85f))
                .clickable { isCreatingNew = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(460.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(hudColors.panelBg)
                    .border(1.5.dp, hudColors.primary, RoundedCornerShape(6.dp))
                    .clickable(enabled = false) {}
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (editingNote != null) "MODIFY DATA MEMO" else "ENCRYPT NEW DATA MEMO",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = formTitle,
                    onValueChange = { formTitle = it.uppercase() },
                    label = { Text("MEMO TITLE", color = hudColors.textMuted, fontSize = 10.sp) },
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
                    value = formContent,
                    onValueChange = { formContent = it.uppercase() },
                    label = { Text("MEMO CONTENT", color = hudColors.textMuted, fontSize = 10.sp) },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = hudColors.textPrimary,
                        unfocusedTextColor = hudColors.textPrimary,
                        focusedBorderColor = hudColors.primary,
                        unfocusedBorderColor = hudColors.border
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FridayHudButton(
                        text = if (formIsPinned) "★ PINNED" else "☆ UNPINNED",
                        isActive = formIsPinned,
                        onClick = { formIsPinned = !formIsPinned },
                        modifier = Modifier.width(110.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FridayHudButton(
                            text = "CANCEL",
                            onClick = { isCreatingNew = false },
                            modifier = Modifier.width(80.dp)
                        )
                        FridayHudButton(
                            text = "SAVE ENTRY",
                            isActive = true,
                            enabled = formTitle.isNotBlank(),
                            onClick = {
                                val item = NoteItem(
                                    id = editingNote?.id ?: UUID.randomUUID().toString(),
                                    title = formTitle.trim(),
                                    content = formContent.trim(),
                                    isPinned = formIsPinned,
                                    timestamp = System.currentTimeMillis()
                                )
                                onSaveNote(item)
                                isCreatingNew = false
                            },
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }
        }
    }
}
