package com.willykez.lumina.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.willykez.lumina.data.MediaItem
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.FullscreenOverlay
import com.willykez.lumina.ui.components.GlassTopBar
import com.willykez.lumina.ui.components.NetImage
import com.willykez.lumina.ui.theme.Amber
import com.willykez.lumina.ui.theme.BgCard
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Neon
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.Rose
import com.willykez.lumina.ui.theme.TextMuted
import com.willykez.lumina.ui.theme.TextPrimary
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class Cluster(
    val name: String,
    val items: List<MediaItem>,
    val color: Color
)

private data class Projected(
    val cluster: Cluster,
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val bubbleScale: Float,
    val alphaVal: Float
)

@Composable
fun ClusterScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val albums by vm.albums.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf<Cluster?>(null) }

    val inf = rememberInfiniteTransition(label = "pulse")
    val pulse by inf.animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    val palette = listOf(
        Primary, Rose, Cyan, Amber, Neon,
        Color(0xFF9B59B6), Color(0xFF1ABC9C), Color(0xFFE67E22)
    )

    val clusters = remember(albums) {
        albums.entries.take(8).mapIndexed { i, (name, items) ->
            Cluster(name, items, palette[i % palette.size])
        }
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        if (expanded == null) {
            MoleculeScene(clusters, pulse) { expanded = it }
        } else {
            ExpandedCluster(expanded!!, { vm.select(it) })
        }

        GlassTopBar(
            title = "Smart Clusters \u25ce",
            onBack = {
                if (expanded != null) expanded = null
                else nav.popBackStack()
            }
        )

        if (albums.isEmpty()) EmptyState("Grant permission to view albums")
        selected?.let { FullscreenOverlay(it) { vm.select(null) } }
    }
}

/**
 * A rotatable 3D "molecule" — each album orbits a glowing nucleus on the
 * surface of a sphere, like atoms bonded around a carbon center. Drag to
 * spin it manually on both axes; it also drifts slowly on its own.
 */
@Composable
private fun MoleculeScene(
    clusters: List<Cluster>,
    pulse: Float,
    onSelect: (Cluster) -> Unit
) {
    var rotY by remember { mutableStateOf(0f) }
    var rotX by remember { mutableStateOf(-14f) }

    // Slow constant idle spin, layered underneath whatever the user drags
    LaunchedEffect(Unit) {
        var lastFrame = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dtSeconds = (now - lastFrame) / 1_000_000_000f
            lastFrame = now
            rotY += 8f * dtSeconds
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    rotY += dragAmount.x * 0.35f
                    rotX = (rotX - dragAmount.y * 0.35f).coerceIn(-75f, 75f)
                }
            }
    ) {
        val density = LocalDensity.current
        val centerXpx = with(density) { (maxWidth / 2).toPx() }
        val centerYpx = with(density) { (maxHeight * 0.46f).toPx() }
        val radiusPx = with(density) { minOf(maxWidth, maxHeight).toPx() } * 0.30f
        val perspective = radiusPx * 2.6f

        val basePoints = remember(clusters.size, radiusPx) {
            fibonacciSpherePoints(clusters.size, radiusPx)
        }

        val projected = remember(clusters, basePoints, rotX, rotY, centerXpx, centerYpx) {
            clusters.mapIndexed { i, cluster ->
                val (bx, by, bz) = basePoints.getOrElse(i) { Triple(0f, 0f, 0f) }
                val (rx, ry, rz) = rotatePoint(bx, by, bz, rotX, rotY)
                val depthT = ((rz + radiusPx) / (2f * radiusPx)).coerceIn(0f, 1f)
                val projScale = perspective / (perspective - rz)
                val sx = centerXpx + rx * projScale
                val sy = centerYpx + ry * projScale
                Projected(
                    cluster = cluster,
                    screenX = sx,
                    screenY = sy,
                    depth = depthT,
                    bubbleScale = 0.55f + depthT * 0.75f,
                    alphaVal = 0.4f + depthT * 0.6f
                )
            }.sortedBy { it.depth }
        }

        Canvas(Modifier.fillMaxSize()) {
            // Equatorial guide ring — squashed by tilt to sell the 3D illusion
            val squash = abs(cos(Math.toRadians(rotX.toDouble())).toFloat()).coerceIn(0.15f, 1f)
            drawOval(
                color = Color.White.copy(alpha = 0.05f),
                topLeft = Offset(centerXpx - radiusPx, centerYpx - radiusPx * squash),
                size = Size(radiusPx * 2f, radiusPx * 2f * squash),
                style = Stroke(width = 1f)
            )

            // Bonds from nucleus to each node, brighter for nodes facing the viewer
            projected.forEach { p ->
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(p.cluster.color.copy(alpha = 0.05f + p.depth * 0.35f), Color.Transparent),
                        Offset(centerXpx, centerYpx), Offset(p.screenX, p.screenY)
                    ),
                    start = Offset(centerXpx, centerYpx),
                    end = Offset(p.screenX, p.screenY),
                    strokeWidth = 1.4f + p.depth * 1.6f
                )
            }

            // Nucleus core
            drawCircle(
                Brush.radialGradient(listOf(Color.White.copy(0.5f), Color.Transparent)),
                radius = 28f * pulse,
                center = Offset(centerXpx, centerYpx)
            )
            drawCircle(
                Color.White.copy(0.9f),
                radius = 9f * pulse,
                center = Offset(centerXpx, centerYpx)
            )
        }

        projected.forEach { p ->
            val bubbleRadiusDp = (34 + p.cluster.items.size.coerceAtMost(120) * 0.22f).dp
            val sizeDp = bubbleRadiusDp * 2 * p.bubbleScale
            val sizePx = with(density) { sizeDp.toPx() }

            val enter = remember(p.cluster.name) { Animatable(0f) }
            LaunchedEffect(p.cluster.name) { enter.animateTo(1f, spring(0.65f, 200f)) }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (p.screenX - sizePx / 2f).toInt(),
                            (p.screenY - sizePx / 2f).toInt()
                        )
                    }
                    .size(sizeDp)
                    .graphicsLayer {
                        alpha = p.alphaVal * enter.value
                        scaleX = enter.value
                        scaleY = enter.value
                    }
                    .clip(RoundedCornerShape(50))
                    .background(p.cluster.color.copy(0.18f))
                    .border(1.dp, p.cluster.color.copy(0.55f), RoundedCornerShape(50))
                    .clickable { onSelect(p.cluster) },
                contentAlignment = Alignment.Center
            ) {
                p.cluster.items.firstOrNull()?.let { item ->
                    NetImage(
                        model = item.uri, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(50))
                            .alpha(0.45f)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        p.cluster.name.take(10),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (9 * p.bubbleScale).coerceIn(6.5f, 10f).sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "${p.cluster.items.size}",
                        color = p.cluster.color,
                        fontWeight = FontWeight.Black,
                        fontSize = (22 * p.bubbleScale).coerceIn(14f, 24f).sp
                    )
                }
            }
        }
    }
}

/** Evenly distributes n points across a sphere's surface (golden-angle spiral). */
private fun fibonacciSpherePoints(n: Int, radius: Float): List<Triple<Float, Float, Float>> {
    if (n <= 0) return emptyList()
    if (n == 1) return listOf(Triple(0f, 0f, radius))
    val points = mutableListOf<Triple<Float, Float, Float>>()
    val goldenAngle = PI * (3.0 - sqrt(5.0))
    for (i in 0 until n) {
        val yFrac = 1f - (i / (n - 1).toFloat()) * 2f
        val ringRadius = sqrt((1f - yFrac * yFrac).coerceAtLeast(0f))
        val theta = goldenAngle * i
        val x = (cos(theta) * ringRadius).toFloat()
        val z = (sin(theta) * ringRadius).toFloat()
        points.add(Triple(x * radius, yFrac * radius, z * radius))
    }
    return points
}

/** Rotates a 3D point first around the X axis, then around the Y axis. */
private fun rotatePoint(x: Float, y: Float, z: Float, rotXDeg: Float, rotYDeg: Float): Triple<Float, Float, Float> {
    val radX = Math.toRadians(rotXDeg.toDouble())
    val radY = Math.toRadians(rotYDeg.toDouble())
    val xd = x.toDouble()
    val yd = y.toDouble()
    val zd = z.toDouble()

    val cosX = cos(radX); val sinX = sin(radX)
    val y1 = yd * cosX - zd * sinX
    val z1 = yd * sinX + zd * cosX

    val cosY = cos(radY); val sinY = sin(radY)
    val x2 = xd * cosY + z1 * sinY
    val z2 = -xd * sinY + z1 * cosY

    return Triple(x2.toFloat(), y1.toFloat(), z2.toFloat())
}

@Composable
private fun ExpandedCluster(cluster: Cluster, onSelect: (MediaItem) -> Unit) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, spring(0.7f, 300f)) }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = enter.value
                scaleX = 0.85f + 0.15f * enter.value
                scaleY = 0.85f + 0.15f * enter.value
            }
            .background(BgCard)
    ) {
        Column(Modifier.fillMaxSize().padding(top = 72.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Brush.horizontalGradient(listOf(cluster.color, cluster.color.copy(0.2f))))
            )
            Spacer(Modifier.height(4.dp))
            Text(
                cluster.name,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 2.dp)
            )
            Text(
                "${cluster.items.size} photos",
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cluster.items) { item ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(116.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelect(item) }
                    ) {
                        NetImage(
                            model = item.uri, contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
