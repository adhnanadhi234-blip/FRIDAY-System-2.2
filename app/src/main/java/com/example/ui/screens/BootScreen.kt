package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationIntensity
import com.example.ui.components.ScanlineAndGridBackground
import com.example.ui.theme.LocalFridayColors
import kotlinx.coroutines.delay

@Composable
fun BootScreen(
    onBootComplete: () -> Unit,
    animationIntensity: AnimationIntensity = AnimationIntensity.MEDIUM
) {
    val hudColors = LocalFridayColors.current

    val bootSequence = listOf(
        "INITIALIZING SYSTEM...",
        "LOADING CORE...",
        "CHECKING HARDWARE...",
        "CHECKING MEMORY...",
        "CHECKING STORAGE...",
        "ESTABLISHING AI CORE...",
        "SYSTEM READY"
    )

    val activeMessages = remember { mutableStateListOf<String>() }
    var isReady by remember { mutableStateOf(false) }
    var bootProgress by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "boot_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        val delayPerStep = when (animationIntensity) {
            AnimationIntensity.LOW -> 180L
            AnimationIntensity.MEDIUM -> 350L
            AnimationIntensity.HIGH -> 500L
        }

        for (i in bootSequence.indices) {
            delay(delayPerStep)
            activeMessages.add(bootSequence[i])
            bootProgress = ((i + 1) * 100) / bootSequence.size
        }
        delay(250)
        isReady = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(hudColors.background)
            .testTag("boot_screen")
            .clickable(enabled = isReady) {
                onBootComplete()
            }
    ) {
        ScanlineAndGridBackground(intensity = animationIntensity)

        // Sci-Fi Reticle and Radar Rings in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = minOf(size.width, size.height) * 0.4f

            drawCircle(
                color = hudColors.primary.copy(alpha = 0.08f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = hudColors.border.copy(alpha = 0.04f),
                radius = baseRadius * 0.7f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Main Boot Container (Landscape Layout)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Logo & System Identity
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FRIDAY SYSTEM",
                    color = hudColors.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "AUTONOMOUS HOLOGRAPHIC INTERFACE // MK-85",
                    color = hudColors.accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORE INIT: $bootProgress%",
                        color = hudColors.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isReady) "READY" else "INITIALIZING",
                        color = if (isReady) hudColors.success else hudColors.primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(hudColors.border.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bootProgress / 100f)
                            .height(6.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(hudColors.secondary, hudColors.primary)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(hudColors.primary.copy(alpha = 0.25f))
                            .border(1.5.dp, hudColors.primary, RoundedCornerShape(4.dp))
                            .clickable { onBootComplete() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TAP TO OPEN / INITIALIZE",
                            color = hudColors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            modifier = Modifier.alpha(pulseAlpha)
                        )
                    }
                }
            }

            // Right Column: Animated Diagnostics Feed
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(hudColors.panelBg)
                    .border(1.dp, hudColors.border.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "DIAGNOSTIC TERMINAL LOG",
                    color = hudColors.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                activeMessages.forEachIndexed { index, msg ->
                    val isLast = index == activeMessages.lastIndex
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "[SYS_${index + 1}0]",
                            color = hudColors.primary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = msg,
                            color = if (isLast && isReady) hudColors.success else hudColors.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
