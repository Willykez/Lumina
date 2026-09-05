package com.willykez.lumina.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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

@Composable
fun HomeScreen(nav: NavController) {
    val inf = rememberInfiniteTransition(label = "orb")
    val orbit by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(24000, easing = LinearEasing)),
        label = "orbit"
    )

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Canvas(Modifier.fillMaxSize()) { drawOrbs(orbit) }

        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Spacer(Modifier.height(28.dp))
            Column(Modifier.padding(horizontal = 24.dp)) {
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
            Spacer(Modifier.height(28.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(modes) { i, m ->
                    ModeCard(m, i) { nav.navigate(m.route) }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(m: Mode, index: Int, onClick: () -> Unit) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 70L)
        enter.animateTo(1f, spring(0.7f, 350f))
    }
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(0.5f, 600f), label = "s")

    Box(
        modifier = Modifier
            .scale(scale * enter.value)
            .alpha(enter.value)
            .fillMaxWidth()
            .height(155.dp)
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
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x55000000))))
        )
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(m.emoji, fontSize = 28.sp, color = Color.White)
            Column {
                Text(m.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(m.sub, color = Color.White.copy(0.7f), fontSize = 11.sp)
            }
        }
    }
}

private fun DrawScope.drawOrbs(angle: Float) {
    val cols = listOf(Color(0x22667EEA), Color(0x22FF6B9D), Color(0x224ECDC4), Color(0x22FFB347))
    cols.forEachIndexed { i, c ->
        val a = Math.toRadians((angle + i * 90.0))
        drawCircle(
            c, 260f, Offset(
                size.width / 2 + cos(a).toFloat() * size.width * 0.32f,
                size.height / 2 + sin(a).toFloat() * size.height * 0.22f
            )
        )
    }
}
