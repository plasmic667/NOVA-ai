package com.example.nova.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nova.voice.VoiceState
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NovaOrb(
    voiceState: VoiceState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NovaOrbAnim")

    // Breathing pulse for IDLE & SPEAKING
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = when (voiceState) {
            VoiceState.LISTENING -> 1.15f
            VoiceState.SPEAKING -> 1.10f
            VoiceState.THINKING -> 1.05f
            VoiceState.IDLE -> 0.98f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.LISTENING -> 800
                    VoiceState.THINKING -> 600
                    VoiceState.SPEAKING -> 500
                    VoiceState.IDLE -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Continuous rotation angle for outer orbital rings & thinking vortex
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.THINKING -> 2000
                    VoiceState.LISTENING -> 4000
                    VoiceState.SPEAKING -> 3000
                    VoiceState.IDLE -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Wave ripple phase for LISTENING / SPEAKING
    val ripplePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripplePhase"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("nova_assistant_orb")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.width / 2f) * 0.55f

            // 1. Outer dynamic soundwaves / halo ripples
            if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                val ripple1Radius = baseRadius + (baseRadius * 0.7f * ripplePhase)
                val ripple1Alpha = (1f - ripplePhase).coerceIn(0f, 1f) * 0.6f
                drawCircle(
                    color = NovaCyan.copy(alpha = ripple1Alpha),
                    radius = ripple1Radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                val phase2 = (ripplePhase + 0.5f) % 1f
                val ripple2Radius = baseRadius + (baseRadius * 0.7f * phase2)
                val ripple2Alpha = (1f - phase2).coerceIn(0f, 1f) * 0.4f
                drawCircle(
                    color = NovaViolet.copy(alpha = ripple2Alpha),
                    radius = ripple2Radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 2. Orbital concentric tech rings
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            NovaCyan.copy(alpha = 0.8f),
                            NovaViolet.copy(alpha = 0.1f),
                            NovaMagenta.copy(alpha = 0.7f),
                            NovaCyan.copy(alpha = 0.8f)
                        ),
                        center = center
                    ),
                    radius = baseRadius * 1.25f * pulseScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Little orbiting satellite nodes
                val nodeCount = 3
                for (i in 0 until nodeCount) {
                    val angleRad = (i * (2 * PI / nodeCount)).toFloat()
                    val nodeDistance = baseRadius * 1.25f * pulseScale
                    val nodeOffset = Offset(
                        center.x + nodeDistance * cos(angleRad),
                        center.y + nodeDistance * sin(angleRad)
                    )
                    drawCircle(
                        color = NovaCyan,
                        radius = 4.dp.toPx(),
                        center = nodeOffset
                    )
                }
            }

            // Counter-rotating inner halo ring
            rotate(-rotationAngle * 1.5f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            NovaViolet.copy(alpha = 0.7f),
                            Color.Transparent,
                            NovaCyan.copy(alpha = 0.6f)
                        ),
                        center = center
                    ),
                    radius = baseRadius * 1.08f * pulseScale,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 3. Ambient outer glow layer
            val glowColor = when (voiceState) {
                VoiceState.LISTENING -> NovaCyan
                VoiceState.THINKING -> NovaViolet
                VoiceState.SPEAKING -> NovaMagenta
                VoiceState.IDLE -> NovaCyan
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.5f),
                        glowColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.4f * pulseScale
                ),
                radius = baseRadius * 1.4f * pulseScale,
                center = center
            )

            // 4. Central luminous core sphere
            val coreBrush = when (voiceState) {
                VoiceState.LISTENING -> Brush.radialGradient(
                    colors = listOf(Color.White, NovaCyan, Color(0xFF003844)),
                    center = center,
                    radius = baseRadius * pulseScale
                )
                VoiceState.THINKING -> Brush.radialGradient(
                    colors = listOf(Color.White, NovaViolet, Color(0xFF2A085C)),
                    center = center,
                    radius = baseRadius * pulseScale
                )
                VoiceState.SPEAKING -> Brush.radialGradient(
                    colors = listOf(Color.White, NovaMagenta, NovaViolet, Color(0xFF090D16)),
                    center = center,
                    radius = baseRadius * pulseScale
                )
                VoiceState.IDLE -> Brush.radialGradient(
                    colors = listOf(Color(0xFFE0F7FA), NovaCyan, Color(0xFF072738)),
                    center = center,
                    radius = baseRadius * pulseScale
                )
            }

            drawCircle(
                brush = coreBrush,
                radius = baseRadius * pulseScale,
                center = center
            )

            // 5. Specular highlight for glass orb depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
                    center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f),
                    radius = baseRadius * 0.5f
                ),
                radius = baseRadius * 0.5f,
                center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f)
            )
        }
    }
}
