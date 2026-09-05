package com.willykez.lumina.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.FullscreenOverlay
import com.willykez.lumina.ui.components.GlassTopBar
import com.willykez.lumina.ui.theme.BgCard
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Divider
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.TextMuted
import com.willykez.lumina.ui.theme.TextPrimary
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission
import kotlin.math.absoluteValue

@Composable
fun LayeredPanelsScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val albums by vm.albums.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val list = remember(albums) { albums.entries.toList() }

    var current by remember { mutableStateOf(0) }
    var drag by remember { mutableStateOf(0f) }
    val aDrag by animateFloatAsState(drag, spring(0.8f, 300f), label = "drag")
    val aCurrent by animateFloatAsState(current.toFloat(), spring(0.75f, 280f), label = "cur")

    val curState = rememberUpdatedState(current)
    val listState = rememberUpdatedState(list.size)

    Box(
        Modifier
            .fillMaxSize()
            .background(BgDeep)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        val c = curState.value
                        val s = listState.value
                        if (drag < -130f && c < s - 1) current = c + 1
                        else if (drag > 130f && c > 0) current = c - 1
                        drag = 0f
                    },
                    onVerticalDrag = { _, d -> drag += d }
                )
            }
    ) {
        list.indices.reversed().forEach { layerIdx ->
            val rel = layerIdx - aCurrent
            if (rel in -1f..4f) {
                val yOff = rel * 58f - aDrag * (if (layerIdx == current) 0.45f else 0.08f)
                val sc = (1f - rel.absoluteValue * 0.045f).coerceIn(0.6f, 1f)
                val al = (1f - rel.absoluteValue * 0.22f).coerceIn(0f, 1f)
                val isTop = layerIdx == current

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = yOff
                            scaleX = sc; scaleY = sc
                            alpha = al
                            shadowElevation = if (isTop) 28f else 4f
                        }
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                        .background(BgCard)
                ) {
                    if (list.isNotEmpty() && layerIdx < list.size) {
                        val entry = list[layerIdx]
                        Column(Modifier.fillMaxSize()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    Modifier
                                        .size(36.dp, 4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Divider)
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth().padding(18.dp, 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        entry.key,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "${entry.value.size} photos",
                                        color = TextMuted,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Text(
                                    "${layerIdx + 1}/${list.size}",
                                    color = Primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Primary.copy(0.12f))
                                        .padding(8.dp, 4.dp)
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Primary, Cyan, androidx.compose.ui.graphics.Color.Transparent)
                                        )
                                    )
                            )
                            if (isTop) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    contentPadding = PaddingValues(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(entry.value.take(60)) { item ->
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(118.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { vm.select(item) }
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
                }
            }
        }

        if (list.isEmpty()) EmptyState("Grant permission to view albums")
        GlassTopBar("Stacked Layers \u29c9", onBack = { nav.popBackStack() })
        selected?.let { FullscreenOverlay(it) { vm.select(null) } }
    }
}
