package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.model.BackgroundTheme

@Composable
fun DynamicAppBackground(
    theme: BackgroundTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_orbs")

    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )

    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 70f,
        targetValue = -70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    val animAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        when (theme) {
            BackgroundTheme.AURORA_BLUE -> {
                // High-res Aurora Fluid Graphic with frosted light overlay
                Image(
                    painter = painterResource(id = R.drawable.img_bg_aurora_blue),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Soft white & electric blue frosted diffusion
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFF8FAFC).copy(alpha = 0.82f),
                                    Color(0xFFF0F6FF).copy(alpha = 0.70f),
                                    Color(0xFFE8F1FF).copy(alpha = 0.88f)
                                )
                            )
                        )
                )
            }

            BackgroundTheme.CYBER_NEON -> {
                // High-res Futuristic Cyber Network Texture
                Image(
                    painter = painterResource(id = R.drawable.img_bg_cyber_neon),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark glass overlay with subtle cyan illumination
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF070B19).copy(alpha = 0.85f),
                                    Color(0xFF0F172A).copy(alpha = 0.75f),
                                    Color(0xFF0A1128).copy(alpha = 0.90f)
                                )
                            )
                        )
                )
            }

            BackgroundTheme.FROST_MINIMAL -> {
                // Crisp White background with animated floating soft blue orbs
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFF1F5F9),
                                    Color(0xFFE8F1FF)
                                )
                            )
                        )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Floating Orb 1 (Electric Blue)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A68FF).copy(alpha = 0.16f * animAlpha),
                                Color(0xFF1A68FF).copy(alpha = 0f)
                            ),
                            center = Offset(w * 0.2f + animOffset1, h * 0.25f + animOffset2),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(w * 0.2f + animOffset1, h * 0.25f + animOffset2)
                    )

                    // Floating Orb 2 (Cyan Glow)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00B4D8).copy(alpha = 0.18f * animAlpha),
                                Color(0xFF00B4D8).copy(alpha = 0f)
                            ),
                            center = Offset(w * 0.8f + animOffset2, h * 0.65f + animOffset1),
                            radius = w * 0.65f
                        ),
                        radius = w * 0.65f,
                        center = Offset(w * 0.8f + animOffset2, h * 0.65f + animOffset1)
                    )
                }
            }

            BackgroundTheme.MIDNIGHT_PULSE -> {
                // Deep Midnight AMOLED Canvas with glowing blue grid & radar circles
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF090D1A))
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Deep central radar glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A68FF).copy(alpha = 0.25f * animAlpha),
                                Color(0xFF00B4D8).copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.35f),
                            radius = w * 0.8f
                        ),
                        radius = w * 0.8f,
                        center = Offset(w * 0.5f, h * 0.35f)
                    )

                    // Secondary Bottom Orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.20f * animAlpha),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.8f + animOffset1, h * 0.85f + animOffset2),
                            radius = w * 0.5f
                        ),
                        radius = w * 0.5f,
                        center = Offset(w * 0.8f + animOffset1, h * 0.85f + animOffset2)
                    )
                }
            }

            BackgroundTheme.ROYAL_CYAN -> {
                // Energetic Ocean & Royal Cyan Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0D2554),
                                    Color(0xFF034078),
                                    Color(0xFF00509D),
                                    Color(0xFF0077B6)
                                )
                            )
                        )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF48CAE4).copy(alpha = 0.30f * animAlpha),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.3f + animOffset2, h * 0.3f + animOffset1),
                            radius = w * 0.6f
                        ),
                        radius = w * 0.6f,
                        center = Offset(w * 0.3f + animOffset2, h * 0.3f + animOffset1)
                    )
                }
            }
        }

        // Render Screen Content on top
        content()
    }
}
