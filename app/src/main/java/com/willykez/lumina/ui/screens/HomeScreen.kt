package com.willykez.lumina.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.Rose
import com.willykez.lumina.ui.theme.TextMuted
import kotlin.math.cos
import kotlin.math.sin

private data class Mode(
    val route: String,
    val title: String,
    val sub: String,
    val emoji: String,
    val grad: List<Color>
)

private val modes = listOf(
    Mode("parallax", "Parallax Depth", "Gyro 3D layers", "\u2726", listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
    Mode("cube", "Cube Carousel", "Swipe to rotate", "\u25c8", listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))),
    Mode("morphing", "Morphing Grid", "Tap & expand fluid", "\u2b21", listOf(Color(0xFF4ECDC4), Color(0xFF44EA72))),
    Mode("canvas", "Infinite Canvas", "Zoom & pan all media", "\u229e", listOf(Color(0xFFFFD93D), Color(0xFFFF6B9D))),
    Mode("layers", "Stacked Layers", "Swipe album sheets", "\u29c9", listOf(Color(0xFF6C63FF), Color(0xFFC25FFF))),
    Mode("mosaic", "Color Mosaic", "Sorted by hue", "\u25c9", listOf(Color(0xFF2DD4BF), Color(0xFF7C3AED))),
    Mode("cluster", "Smart Clusters", "Grouped by album", "\u25ce", listOf(Color(0xFFEC4899), Color(0xFFF59E0B)))
)

// Fixed pseudo-random twinkle field, computed once so it doesn't reshuffle on recomposition
private val sparkles = List(28) { i ->
    val fx = ((i * 53 + 7) % 100) / 100f
    val fy = ((i * 91 + 31) % 100) / 100f
    val phase = (i * 37 % 100) / 100f
    Triple(fx, fy, phase)
}

@Composable
fun HomeScreen(nav: NavController) {
    val haptics = LocalHapticFeedback.current

    val inf = rememberInfiniteTransition(label = "bg")
    val orbit by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "orbit"
    )
    val twinkle by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "twinkle"
    )
    val hueShift by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "hue"
    )

    // Logo entrance
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.82f) }
    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(500))
    }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 260f))
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Canvas(Modifier.fillMaxSize()) {
            drawAurora(orbit, hueShift)
            drawSparkles(twinkle)
        }

        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Spacer(Modifier.height(28.dp))
            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            ) {
                Text(
                    "LUMINA",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.linearGradient(listOf(Primary, Rose, Cyan)),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp
                    )
                )
                Text(
                    "Extraordinary Gallery",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(24.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(modes) { i, m ->
                    ModeCard(m, i) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        nav.navigate(m.route)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(m: Mode, index: Int, onClick: () -> Unit) {
    // Staggered spring entrance with a subtle rotational "settle"
    val enter = remember { Animatable(0f) }
    val rotEnter = remember { Animatable(if (index % 2 == 0) -8f else 8f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 65L)
        enter.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 320f))
        rotEnter.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = 260f))
    }

    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(0.5f, 600f), label = "press")

    // Looping diagonal shimmer sweep across every card, staggered by index
    val inf = rememberInfiniteTransition(label = "cardShimmer$index")
    val sweep by inf.animateFloat(
        -0.4f, 1.4f,
        infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "sweep"
    )
    val floatEmoji by inf.animateFloat(
        -3f, 3f,
        infiniteRepeatable(tween(1900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatEmoji"
    )

    Box(
        modifier = Modifier
            .scale(pressScale * (0.9f + 0.1f * enter.value))
            .alpha(enter.value)
            .fillMaxWidth()
            .height(155.dp)
            .graphicsLayer {
                rotationZ = rotEnter.value
                translationY = (1f - enter.value) * 40f
                cameraDistance = 10f * density
            }
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(m.grad))
            .clickable(interactionSource = src, indication = null) { onClick() }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Color.White.copy(0.09f),
                size.width * 0.75f,
                Offset(size.width * 1.1f, -size.height * 0.2f)
            )
            drawCircle(Color.White.copy(0.05f), size.width * 0.4f, Offset(0f, size.height))
            // shimmer sweep band
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(0.14f), Color.Transparent),
                    start = Offset(sweep * size.width - 60f, 0f),
                    end = Offset(sweep * size.width + 60f, size.height)
                )
            )
        }
        if (pressed) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(Color.White.copy(0.16f), Color.Transparent)))
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x55000000))))
        )
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                m.emoji,
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.graphicsLayer { translationY = floatEmoji }
            )
            Column {
                Text(m.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(m.sub, color = Color.White.copy(0.7f), fontSize = 11.sp)
            }
        }
    }
}

private fun DrawScope.drawAurora(angle: Float, hueShift: Float) {
    val palette = listOf(
        Color(0xFF667EEA), Color(0xFFFF6B9D), Color(0xFF4ECDC4),
        Color(0xFFFFB347), Color(0xFF9B59B6)
    )
    palette.forEachIndexed { i, base ->
        val phase = Math.toRadians((angle + i * 72.0 + hueShift * 40.0))
        val radiusFactor = 0.28f + 0.05f * sin(phase * 1.3).toFloat()
        drawCircle(
            base.copy(alpha = 0.10f),
            size.minDimension * 0.42f,
            Offset(
                size.width / 2 + cos(phase).toFloat() * size.width * radiusFactor,
                size.height / 2 + sin(phase).toFloat() * size.height * (radiusFactor * 0.75f)
            )
        )
    }
}

private fun DrawScope.drawSparkles(twinkle: Float) {
    sparkles.forEach { (fx, fy, phase) ->
        val local = ((twinkle + phase) % 1f)
        val a = (sin(local * Math.PI * 2).toFloat() * 0.5f + 0.5f) * 0.55f
        drawCircle(
            Color.White.copy(alpha = a.coerceIn(0f, 1f)),
            1.4f,
            Offset(fx * size.width, fy * size.height)
        )
    }
}
