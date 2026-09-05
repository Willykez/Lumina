package com.willykez.lumina.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.GlassTopBar
import com.willykez.lumina.ui.theme.Amber
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Neon
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.Rose
import com.willykez.lumina.ui.theme.TextMuted
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission
import kotlin.math.abs

@Composable
fun CubeCarouselScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val images by vm.images.collectAsStateWithLifecycle()

    var page by remember { mutableStateOf(0) }
    var rawDrag by remember { mutableStateOf(0f) }
    val animDrag by animateFloatAsState(rawDrag, spring(0.82f, 380f), label = "drag")

    val pageState = rememberUpdatedState(page)
    val sizeState = rememberUpdatedState(images.size)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val p = pageState.value
                        val s = sizeState.value
                        if (rawDrag < -120f && p < s - 1) page = p + 1
                        else if (rawDrag > 120f && p > 0) page = p - 1
                        rawDrag = 0f
                    },
                    onHorizontalDrag = { _, d -> rawDrag += d }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) { drawCubeGlow(page) }

        if (images.isNotEmpty()) {
            BoxWithConstraints(Modifier.fillMaxSize().padding(top = 64.dp, bottom = 80.dp)) {
                val density = LocalDensity.current
                val wPx = with(density) { maxWidth.toPx() }
                val progress = (animDrag / wPx.coerceAtLeast(1f)).coerceIn(-1f, 1f)

                images.getOrNull(page)?.let { item ->
                    val rotY = progress * 90f
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 22.dp)
                            .fillMaxSize()
                            .graphicsLayer {
                                transformOrigin = if (progress < 0) TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)
                                rotationY = rotY
                                cameraDistance = 9f * density.density
                                alpha = 1f - abs(progress) * 0.3f
                            }
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(
                            model = item.uri, contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f))))
                        )
                        Box(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text(
                                "${page + 1} / ${images.size}",
                                color = Color.White.copy(0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                val incomingIdx = if (progress < 0) page + 1 else page - 1
                images.getOrNull(incomingIdx)?.let { item ->
                    val inProg = if (progress < 0) 1f + progress else progress - 1f
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 22.dp)
                            .fillMaxSize()
                            .graphicsLayer {
                                transformOrigin = if (progress < 0) TransformOrigin(0f, 0.5f) else TransformOrigin(1f, 0.5f)
                                rotationY = inProg * 90f
                                cameraDistance = 9f * density.density
                                alpha = 1f - abs(inProg) * 0.3f
                            }
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        AsyncImage(
                            model = item.uri, contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (images.isEmpty()) EmptyState("Grant permission to view photos")

        GlassTopBar("Cube Carousel \u25c8", onBack = { nav.popBackStack() })

        if (images.isNotEmpty()) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(minOf(images.size, 8)) { i ->
                    val active = i == page % 8
                    Box(
                        Modifier
                            .size(if (active) 20.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (active) Primary else TextMuted.copy(0.35f))
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCubeGlow(page: Int) {
    val colors = listOf(Primary, Rose, Cyan, Amber, Neon)
    val c = colors[page % colors.size]
    drawCircle(c.copy(0.08f), size.width * 0.6f, Offset(size.width / 2, size.height / 2))
}
