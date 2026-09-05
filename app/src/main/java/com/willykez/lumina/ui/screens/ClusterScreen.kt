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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.willykez.lumina.data.MediaItem
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.FullscreenOverlay
import com.willykez.lumina.ui.components.GlassTopBar
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
import kotlin.math.cos
import kotlin.math.sin

private data class Cluster(
    val name: String,
    val items: List<MediaItem>,
    val fx: Float,
    val fy: Float,
    val color: Color
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
        val entries = albums.entries.take(8)
        entries.mapIndexed { i, (name, items) ->
            val angle = (i.toFloat() / entries.size.coerceAtLeast(1)) * 2f * PI.toFloat()
            Cluster(
                name, items,
                0.5f + 0.28f * cos(angle),
                0.45f + 0.28f * sin(angle),
                palette[i % palette.size]
            )
        }
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        if (expanded == null) {
            Canvas(Modifier.fillMaxSize()) { drawWeb(clusters) }
            clusters.forEach { cluster ->
                ClusterBubble(cluster, pulse) { expanded = cluster }
            }
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

@Composable
private fun ClusterBubble(cluster: Cluster, pulse: Float, onClick: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val r = (42 + cluster.items.size.coerceAtMost(120) * 0.28f).dp
        val cx = maxWidth * cluster.fx - r
        val cy = maxHeight * cluster.fy - r

        val enter = remember { Animatable(0f) }
        LaunchedEffect(Unit) { enter.animateTo(1f, spring(0.65f, 200f)) }

        Box(
            modifier = Modifier
                .offset(cx, cy)
                .size(r * 2)
                .graphicsLayer {
                    scaleX = pulse * enter.value
                    scaleY = pulse * enter.value
                }
                .clip(RoundedCornerShape(50))
                .background(cluster.color.copy(0.15f))
                .border(1.5.dp, cluster.color.copy(0.5f), RoundedCornerShape(50))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            cluster.items.firstOrNull()?.let { item ->
                AsyncImage(
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
                    cluster.name.take(10),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "${cluster.items.size}",
                    color = cluster.color,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
        }
    }
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
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp),
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
                        AsyncImage(
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

private fun DrawScope.drawWeb(clusters: List<Cluster>) {
    val cx = size.width / 2f
    val cy = size.height * 0.45f
    clusters.forEach { c ->
        val x = c.fx * size.width
        val y = c.fy * size.height
        drawLine(
            brush = Brush.linearGradient(
                listOf(c.color.copy(0.25f), Color.Transparent),
                Offset(x, y), Offset(cx, cy)
            ),
            start = Offset(x, y),
            end = Offset(cx, cy),
            strokeWidth = 1.2f
        )
    }
    drawCircle(Color.White.copy(0.06f), 24f, Offset(cx, cy))
}
