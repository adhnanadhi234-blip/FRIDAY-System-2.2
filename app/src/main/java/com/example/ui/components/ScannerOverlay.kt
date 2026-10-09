package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AnimationIntensity
import com.example.ui.theme.LocalFridayColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ScannerOverlay
 *
 * A futuristic holographic HUD scanning interface composable that draws animated,
 * glowing geometric shapes including:
 * - Counter-rotating concentric holographic reticle rings and segmented telemetry arcs
 * - Animated rotating hex/octagonal targeting prisms
 * - Expanding sonar pulse waves
 * - Sweeping radar beam with trailing gradient fade
 * - Corner bracket lock-on targets with diamond nodes and technical crosshairs
 */
@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    intensity: AnimationIntensity = AnimationIntensity.MEDIUM,
    scanColor: Color? = null,
    accentColor: Color? = null,
    showTargetReticles: Boolean = true,
    showSweepingBeam: Boolean = true,
    showHexagonGrid: Boolean = true
) {
    val hudColors = LocalFridayColors.current
    val primaryColor = scanColor ?: hudColors.primary
    val secondaryColor = accentColor ?: hudColors.accent

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_overlay_anim")

    // Rotation speeds based on intensity
    val rotDuration = when (intensity) {
        AnimationIntensity.LOW -> 16000
        AnimationIntensity.MEDIUM -> 9000
        AnimationIntensity.HIGH -> 5000
    }

    // Outer ring clockwise rotation
    val angleClockwise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_rot_cw"
    )

    // Inner hex counter-clockwise rotation
    val angleCounterClockwise by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween((rotDuration * 0.75f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_rot_ccw"
    )

    // Expanding sonar pulse wave
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (intensity == AnimationIntensity.HIGH) 2200 else 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_pulse"
    )

    // Secondary pulse with offset
    val pulse2Progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (intensity == AnimationIntensity.HIGH) 2200 else 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_pulse2"
    )

    // Scanning vertical bar sweep
    val sweepYProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (intensity == AnimationIntensity.HIGH) 3000 else 5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanner_sweep_y"
    )

    // Dynamic glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanner_glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("scanner_overlay")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val maxRadius = minOf(w, h) * 0.46f

            if (maxRadius <= 0f) return@Canvas

            // 1. Expanding Holographic Sonar Pulses
            if (intensity != AnimationIntensity.LOW) {
                val r1 = maxRadius * pulseProgress
                val alpha1 = (1f - pulseProgress).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = primaryColor.copy(alpha = alpha1),
                    radius = r1,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                val shiftedProgress = (pulse2Progress + 0.5f) % 1f
                val r2 = maxRadius * shiftedProgress
                val alpha2 = (1f - shiftedProgress).coerceIn(0f, 1f) * 0.35f
                drawCircle(
                    color = secondaryColor.copy(alpha = alpha2),
                    radius = r2,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Center Crosshairs with Ticks
            val crosshairLen = maxRadius * 0.95f
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x - crosshairLen, center.y),
                end = Offset(center.x + crosshairLen, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - crosshairLen),
                end = Offset(center.x, center.y + crosshairLen),
                strokeWidth = 1.dp.toPx()
            )

            // Crosshair tick marks along X and Y axes
            val tickCount = 6
            for (i in 1..tickCount) {
                val dist = (crosshairLen / tickCount) * i
                val tickSize = if (i % 2 == 0) 5.dp.toPx() else 3.dp.toPx()

                // Horizontal ticks
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(center.x + dist, center.y - tickSize),
                    end = Offset(center.x + dist, center.y + tickSize),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(center.x - dist, center.y - tickSize),
                    end = Offset(center.x - dist, center.y + tickSize),
                    strokeWidth = 1.dp.toPx()
                )

                // Vertical ticks
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(center.x - tickSize, center.y + dist),
                    end = Offset(center.x + tickSize, center.y + dist),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(center.x - tickSize, center.y - dist),
                    end = Offset(center.x + tickSize, center.y - dist),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Outer Concentric Telemetry Rings & Segmented Arcs
            val rOuter = maxRadius * 0.95f
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = rOuter,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Segmented Rotating Outer Arc Set (4 quadrants with gaps)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
            val arcRect = Size(rOuter * 2, rOuter * 2)
            val arcTopLeft = Offset(center.x - rOuter, center.y - rOuter)

            drawArc(
                color = primaryColor.copy(alpha = 0.6f * glowAlpha),
                startAngle = angleClockwise,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect, cap = StrokeCap.Round)
            )
            drawArc(
                color = secondaryColor.copy(alpha = 0.6f * glowAlpha),
                startAngle = angleClockwise + 90f,
                sweepAngle = 60f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor.copy(alpha = 0.6f * glowAlpha),
                startAngle = angleClockwise + 180f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect, cap = StrokeCap.Round)
            )
            drawArc(
                color = secondaryColor.copy(alpha = 0.6f * glowAlpha),
                startAngle = angleClockwise + 270f,
                sweepAngle = 60f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Middle Ring with Degree Marks (36 ticks at 10-degree increments)
            val rMid = maxRadius * 0.72f
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = rMid,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            val tickStepDeg = 15
            for (deg in 0 until 360 step tickStepDeg) {
                val rad = Math.toRadians((deg + angleClockwise * 0.5f).toDouble())
                val isMajor = deg % 45 == 0
                val tickLen = if (isMajor) 7.dp.toPx() else 3.5.dp.toPx()
                val startX = center.x + ((rMid - tickLen) * cos(rad)).toFloat()
                val startY = center.y + ((rMid - tickLen) * sin(rad)).toFloat()
                val endX = center.x + (rMid * cos(rad)).toFloat()
                val endY = center.y + (rMid * sin(rad)).toFloat()

                drawLine(
                    color = if (isMajor) secondaryColor.copy(alpha = 0.7f) else primaryColor.copy(alpha = 0.35f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                )
            }

            // 5. Hexagonal Targeting Prisms
            if (showHexagonGrid) {
                // Outer Hexagon
                drawHolographicPolygon(
                    center = center,
                    radius = maxRadius * 0.52f,
                    sides = 6,
                    rotationDegrees = angleCounterClockwise,
                    color = primaryColor.copy(alpha = 0.45f),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Inner Hexagon
                drawHolographicPolygon(
                    center = center,
                    radius = maxRadius * 0.36f,
                    sides = 6,
                    rotationDegrees = angleClockwise * 1.2f,
                    color = secondaryColor.copy(alpha = 0.55f * glowAlpha),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Core Diamond (4 sides)
                drawHolographicPolygon(
                    center = center,
                    radius = maxRadius * 0.18f,
                    sides = 4,
                    rotationDegrees = angleCounterClockwise * 1.5f,
                    color = primaryColor.copy(alpha = 0.8f * glowAlpha),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 6. Central Core Target Pip
            drawCircle(
                color = primaryColor,
                radius = 3.dp.toPx(),
                center = center
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 7.dp.toPx(),
                center = center
            )

            // 7. Sweeping Radar Beam with Trailing Gradient Sector
            if (showSweepingBeam && intensity != AnimationIntensity.LOW) {
                val sweepRad = Math.toRadians(angleClockwise.toDouble())
                val beamEndX = center.x + (maxRadius * 0.95f * cos(sweepRad)).toFloat()
                val beamEndY = center.y + (maxRadius * 0.95f * sin(sweepRad)).toFloat()

                // Radar leading edge line
                drawLine(
                    color = primaryColor.copy(alpha = 0.8f),
                    start = center,
                    end = Offset(beamEndX, beamEndY),
                    strokeWidth = 2.dp.toPx()
                )

                // Trailing sector (approximate sweep glow via sector arc)
                val trailArcRect = Size(maxRadius * 1.9f, maxRadius * 1.9f)
                val trailTopLeft = Offset(center.x - maxRadius * 0.95f, center.y - maxRadius * 0.95f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryColor.copy(alpha = 0.03f),
                            primaryColor.copy(alpha = 0.15f),
                            primaryColor.copy(alpha = 0.35f)
                        ),
                        center = center
                    ),
                    startAngle = (angleClockwise - 45f),
                    sweepAngle = 45f,
                    useCenter = true,
                    topLeft = trailTopLeft,
                    size = trailArcRect
                )
            }

            // 8. Scanning Laser Line Across the Reticle
            if (intensity != AnimationIntensity.LOW) {
                val scanLineY = center.y - maxRadius * 0.8f + (maxRadius * 1.6f * sweepYProgress)
                val halfScanWidth = maxRadius * 0.85f

                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            secondaryColor.copy(alpha = 0.3f),
                            secondaryColor.copy(alpha = 0.8f * glowAlpha),
                            secondaryColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startX = center.x - halfScanWidth,
                        endX = center.x + halfScanWidth
                    ),
                    start = Offset(center.x - halfScanWidth, scanLineY),
                    end = Offset(center.x + halfScanWidth, scanLineY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 9. Floating Tactical Lock-on Targets with Corner Brackets
            if (showTargetReticles) {
                // Target 1: Top-Right sector
                val t1Offset = Offset(center.x + maxRadius * 0.48f, center.y - maxRadius * 0.42f)
                drawTargetLockBox(
                    center = t1Offset,
                    boxSize = 22.dp.toPx(),
                    bracketLen = 6.dp.toPx(),
                    color = secondaryColor.copy(alpha = 0.85f * glowAlpha),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Target 2: Bottom-Left sector
                val t2Offset = Offset(center.x - maxRadius * 0.52f, center.y + maxRadius * 0.38f)
                drawTargetLockBox(
                    center = t2Offset,
                    boxSize = 18.dp.toPx(),
                    bracketLen = 5.dp.toPx(),
                    color = primaryColor.copy(alpha = 0.75f * glowAlpha),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}

/**
 * Draws an animated regular polygon (hexagon, octagon, diamond, etc.)
 */
private fun DrawScope.drawHolographicPolygon(
    center: Offset,
    radius: Float,
    sides: Int,
    rotationDegrees: Float,
    color: Color,
    strokeWidth: Float
) {
    if (sides < 3 || radius <= 0f) return

    val path = Path()
    val angleStep = (2 * PI / sides).toFloat()
    val startAngleRad = Math.toRadians(rotationDegrees.toDouble()).toFloat()

    for (i in 0 until sides) {
        val a = startAngleRad + i * angleStep
        val x = center.x + radius * cos(a)
        val y = center.y + radius * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Corner nodes (subtle glowing dots on vertices)
    for (i in 0 until sides) {
        val a = startAngleRad + i * angleStep
        val x = center.x + radius * cos(a)
        val y = center.y + radius * sin(a)
        drawCircle(
            color = color,
            radius = strokeWidth * 1.2f,
            center = Offset(x, y)
        )
    }
}

/**
 * Draws a high-tech corner-bracketed target lock box [ ] with center pip
 */
private fun DrawScope.drawTargetLockBox(
    center: Offset,
    boxSize: Float,
    bracketLen: Float,
    color: Color,
    strokeWidth: Float
) {
    val half = boxSize / 2f
    val l = center.x - half
    val r = center.x + half
    val t = center.y - half
    val b = center.y + half

    // Top-left
    drawLine(color, Offset(l, t), Offset(l + bracketLen, t), strokeWidth)
    drawLine(color, Offset(l, t), Offset(l, t + bracketLen), strokeWidth)

    // Top-right
    drawLine(color, Offset(r - bracketLen, t), Offset(r, t), strokeWidth)
    drawLine(color, Offset(r, t), Offset(r, t + bracketLen), strokeWidth)

    // Bottom-left
    drawLine(color, Offset(l, b - bracketLen), Offset(l, b), strokeWidth)
    drawLine(color, Offset(l, b), Offset(l + bracketLen, b), strokeWidth)

    // Bottom-right
    drawLine(color, Offset(r - bracketLen, b), Offset(r, b), strokeWidth)
    drawLine(color, Offset(r, b - bracketLen), Offset(r, b), strokeWidth)

    // Center target pip
    drawCircle(color, radius = 2.dp.toPx(), center = center)
}
