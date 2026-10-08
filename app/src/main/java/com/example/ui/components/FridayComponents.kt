package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationIntensity
import com.example.ui.theme.LocalFridayColors

@Composable
fun ScanlineAndGridBackground(
    intensity: AnimationIntensity = AnimationIntensity.MEDIUM,
    modifier: Modifier = Modifier
) {
    val hudColors = LocalFridayColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")

    val scanLineY = if (intensity != AnimationIntensity.LOW) {
        val duration = if (intensity == AnimationIntensity.HIGH) 3500 else 6000
        val anim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "scan_anim"
        )
        anim
    } else 0f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val step = 48.dp.toPx()

        // Tech grid lines
        var x = 0f
        while (x < w) {
            drawLine(
                color = hudColors.border.copy(alpha = 0.04f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < h) {
            drawLine(
                color = hudColors.border.copy(alpha = 0.04f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += step
        }

        // Animated scan line
        if (intensity != AnimationIntensity.LOW) {
            val curY = scanLineY * h
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        hudColors.primary.copy(alpha = 0.35f),
                        hudColors.primary.copy(alpha = 0.7f),
                        hudColors.primary.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, curY),
                end = Offset(w, curY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun FridayHudPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    statusText: String? = null,
    borderColor: Color = LocalFridayColors.current.border,
    backgroundColor: Color = LocalFridayColors.current.panelBg,
    content: @Composable () -> Unit
) {
    val hudColors = LocalFridayColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
    ) {
        // Tech Corner Brackets Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bracketSize = 10.dp.toPx()
            val strokeW = 2.dp.toPx()
            val color = borderColor

            // Top Left bracket
            drawLine(color, Offset(0f, 0f), Offset(bracketSize, 0f), strokeW)
            drawLine(color, Offset(0f, 0f), Offset(0f, bracketSize), strokeW)

            // Top Right bracket
            drawLine(color, Offset(size.width - bracketSize, 0f), Offset(size.width, 0f), strokeW)
            drawLine(color, Offset(size.width, 0f), Offset(size.width, bracketSize), strokeW)

            // Bottom Left bracket
            drawLine(color, Offset(0f, size.height - bracketSize), Offset(0f, size.height), strokeW)
            drawLine(color, Offset(0f, size.height), Offset(bracketSize, size.height), strokeW)

            // Bottom Right bracket
            drawLine(color, Offset(size.width - bracketSize, size.height), Offset(size.width, size.height), strokeW)
            drawLine(color, Offset(size.width, size.height - bracketSize), Offset(size.width, size.height), strokeW)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(hudColors.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        color = hudColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (statusText != null) {
                        Text(
                            text = statusText.uppercase(),
                            color = hudColors.accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun FridayHudButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "friday_button",
    isActive: Boolean = false,
    isWarning: Boolean = false,
    enabled: Boolean = true
) {
    val hudColors = LocalFridayColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = when {
        isWarning -> hudColors.warning
        isActive -> hudColors.accent
        else -> hudColors.primary
    }

    val bgAlpha = when {
        !enabled -> 0.05f
        isPressed -> 0.45f
        isActive -> 0.28f
        else -> 0.12f
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(baseColor.copy(alpha = bgAlpha))
            .border(
                width = if (isActive || isPressed) 1.5.dp else 1.dp,
                color = if (enabled) baseColor.copy(alpha = if (isActive || isPressed) 0.9f else 0.5f) else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (enabled) (if (isActive) hudColors.textPrimary else baseColor) else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HudTelemetryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = LocalFridayColors.current.textSecondary,
    accentValue: Boolean = false
) {
    val hudColors = LocalFridayColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            color = hudColors.textMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value.uppercase(),
            color = if (accentValue) hudColors.primary else valueColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HudSteppedProgressBar(
    progressPercent: Int,
    totalBars: Int = 12,
    modifier: Modifier = Modifier,
    fillColor: Color = LocalFridayColors.current.primary
) {
    val hudColors = LocalFridayColors.current
    val activeBars = ((progressPercent.coerceIn(0, 100) / 100f) * totalBars).toInt()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 0 until totalBars) {
            val isActive = i < activeBars
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        if (isActive) fillColor else hudColors.border.copy(alpha = 0.15f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun FridayKeypad(
    onDigitPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    onClearPress: () -> Unit,
    onEnterPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudColors = LocalFridayColors.current

    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("CLR", "0", "DEL")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in keys) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (key in row) {
                    val isAction = key == "CLR" || key == "DEL"
                    Box(
                        modifier = Modifier
                            .testTag("keypad_$key")
                            .size(width = 64.dp, height = 44.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(hudColors.primary.copy(alpha = if (isAction) 0.15f else 0.08f))
                            .border(
                                1.dp,
                                if (isAction) hudColors.secondary.copy(alpha = 0.6f) else hudColors.border.copy(alpha = 0.4f),
                                RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                when (key) {
                                    "CLR" -> onClearPress()
                                    "DEL" -> onDeletePress()
                                    else -> onDigitPress(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = if (isAction) hudColors.accent else hudColors.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ENTER button
        Box(
            modifier = Modifier
                .testTag("keypad_enter")
                .width(208.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(hudColors.primary.copy(alpha = 0.25f))
                .border(1.dp, hudColors.primary, RoundedCornerShape(4.dp))
                .clickable { onEnterPress() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ENTER // VERIFY".uppercase(),
                color = hudColors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
