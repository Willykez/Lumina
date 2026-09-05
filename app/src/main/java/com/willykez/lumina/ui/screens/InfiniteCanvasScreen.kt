package com.willykez.lumina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.willykez.lumina.data.MediaItem
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.FullscreenOverlay
import com.willykez.lumina.ui.components.GlassTopBar
import com.willykez.lumina.ui.components.NetImage
import com.willykez.lumina.ui.theme.BgCard
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Divider
import com.willykez.lumina.ui.theme.TextMuted
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission

private data class CanvasNode(val item: MediaItem, val x: Float, val y: Float, val size: Dp)

@Composable
fun InfiniteCanvasScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val images by vm.images.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()

    var scale by remember { mutableStateOf(0.72f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val txState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.15f, 4f)
        offset += panChange
    }

    val nodes = remember(images.size) {
        images.take(100).mapIndexed { i, item ->
            val col = i % 9
            val row = i / 9
            val jx = ((i * 73 + 31) % 50 - 25).toFloat()
            val jy = ((i * 47 + 19) % 50 - 25).toFloat()
            CanvasNode(item, col * 200f + jx, row * 220f + jy, (90 + i * 33 % 80).dp)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BgDeep)
            .transformable(txState)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    translationX = offset.x; translationY = offset.y
                }
        ) {
            nodes.forEach { node ->
                Box(
                    modifier = Modifier
                        .offset { IntOffset(node.x.toInt(), node.y.toInt()) }
                        .size(node.size)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Divider.copy(0.4f), RoundedCornerShape(10.dp))
                        .clickable { vm.select(node.item) }
                ) {
                    NetImage(
                        model = node.item.uri, contentDescription = node.item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        GlassTopBar("Infinite Canvas \u229e", onBack = { nav.popBackStack() })

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(10.dp),
            color = BgCard.copy(0.9f)
        ) {
            Text(
                "${(scale * 100).toInt()}%",
                modifier = Modifier.padding(10.dp, 5.dp),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(10.dp),
            color = BgCard.copy(0.9f)
        ) {
            Text(
                "Pinch \u00b7 Pan \u00b7 Tap",
                modifier = Modifier.padding(10.dp, 5.dp),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (images.isEmpty()) EmptyState("Grant permission to view photos")
        selected?.let { FullscreenOverlay(it) { vm.select(null) } }
    }
}
