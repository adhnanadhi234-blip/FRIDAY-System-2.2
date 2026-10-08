package com.example.ui.screens.subsystems

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FridayHudButton
import com.example.ui.theme.LocalFridayColors

data class DrawStroke(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

@Composable
fun BlackBoardPanel(
    onClosePanel: () -> Unit
) {
    val hudColors = LocalFridayColors.current

    val strokes = remember { mutableStateListOf<DrawStroke>() }
    val undoneStrokes = remember { mutableStateListOf<DrawStroke>() }

    var selectedColor by remember { mutableStateOf(hudColors.primary) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraserMode by remember { mutableStateOf(false) }

    var currentPath by remember { mutableStateOf<Path?>(null) }

    val palette = listOf(
        hudColors.primary,
        hudColors.accent,
        Color(0xFFFFD700), // Gold
        Color(0xFFFF3344), // Crimson
        Color(0xFF00FF9D), // Emerald
        Color(0xFFFFFFFF)  // White
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("blackboard_panel")
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HOLOGRAPHIC TACTICAL BLACKBOARD",
                    color = hudColors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isEraserMode) "TOOL: ERASER" else "TOOL: HOLOGRAPHIC PEN",
                    color = if (isEraserMode) hudColors.warning else hudColors.accent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FridayHudButton(
                    text = "UNDO",
                    enabled = strokes.isNotEmpty(),
                    onClick = {
                        if (strokes.isNotEmpty()) {
                            val removed = strokes.removeAt(strokes.lastIndex)
                            undoneStrokes.add(removed)
                        }
                    },
                    modifier = Modifier.width(70.dp)
                )
                FridayHudButton(
                    text = "REDO",
                    enabled = undoneStrokes.isNotEmpty(),
                    onClick = {
                        if (undoneStrokes.isNotEmpty()) {
                            val restored = undoneStrokes.removeAt(undoneStrokes.lastIndex)
                            strokes.add(restored)
                        }
                    },
                    modifier = Modifier.width(70.dp)
                )
                FridayHudButton(
                    text = "CLEAR",
                    isWarning = true,
                    enabled = strokes.isNotEmpty(),
                    onClick = {
                        strokes.clear()
                        undoneStrokes.clear()
                    },
                    modifier = Modifier.width(70.dp)
                )
                FridayHudButton(
                    text = "RETURN",
                    onClick = onClosePanel,
                    modifier = Modifier.width(80.dp)
                )
            }
        }

        // Sub-toolbar: Colors, Pen/Eraser toggle, Brush size
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(hudColors.panelBg)
                .border(1.dp, hudColors.border.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colors
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COLOR:",
                    color = hudColors.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                palette.forEach { c ->
                    val isSelected = selectedColor == c && !isEraserMode
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColor = c
                                isEraserMode = false
                            }
                    )
                }
            }

            // Eraser Mode Toggle
            FridayHudButton(
                text = if (isEraserMode) "ERASER ON" else "ERASER",
                isActive = isEraserMode,
                onClick = { isEraserMode = !isEraserMode },
                modifier = Modifier.width(95.dp)
            )

            // Brush Size
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "SIZE: ${strokeWidth.toInt()}PX",
                    color = hudColors.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 2f..30f,
                    modifier = Modifier.width(90.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = hudColors.primary,
                        activeTrackColor = hudColors.primary,
                        inactiveTrackColor = hudColors.border.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Holographic Drawing Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF040A12))
                .border(1.5.dp, hudColors.primary.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isEraserMode, selectedColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val p = Path().apply { moveTo(offset.x, offset.y) }
                                currentPath = p
                                val s = DrawStroke(
                                    path = p,
                                    color = if (isEraserMode) Color(0xFF040A12) else selectedColor,
                                    strokeWidth = if (isEraserMode) strokeWidth * 2.5f else strokeWidth,
                                    isEraser = isEraserMode
                                )
                                strokes.add(s)
                                undoneStrokes.clear()
                            },
                            onDrag = { change, _ ->
                                currentPath?.lineTo(change.position.x, change.position.y)
                                // Trigger redraw by replacing the last item with itself
                                if (strokes.isNotEmpty()) {
                                    val last = strokes.removeAt(strokes.lastIndex)
                                    strokes.add(last)
                                }
                            },
                            onDragEnd = {
                                currentPath = null
                            },
                            onDragCancel = {
                                currentPath = null
                            }
                        )
                    }
            ) {
                // Background subtle holographic grid
                val step = 32.dp.toPx()
                var gx = 0f
                while (gx < size.width) {
                    drawLine(
                        color = hudColors.border.copy(alpha = 0.05f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, size.height),
                        strokeWidth = 1f
                    )
                    gx += step
                }
                var gy = 0f
                while (gy < size.height) {
                    drawLine(
                        color = hudColors.border.copy(alpha = 0.05f),
                        start = Offset(0f, gy),
                        end = Offset(size.width, gy),
                        strokeWidth = 1f
                    )
                    gy += step
                }

                // Render all strokes
                for (s in strokes) {
                    drawPath(
                        path = s.path,
                        color = s.color,
                        style = Stroke(
                            width = s.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
