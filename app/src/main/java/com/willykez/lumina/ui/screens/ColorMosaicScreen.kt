package com.willykez.lumina.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.willykez.lumina.data.MediaItem
import com.willykez.lumina.ui.components.EaseOutBack
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.FullscreenOverlay
import com.willykez.lumina.ui.components.GlassTopBar
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission

private fun MediaItem.hue(): Float =
    (name.hashCode().let { if (it < 0) -it else it } % 360).toFloat()

@Composable
fun ColorMosaicScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val images by vm.images.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    var compact by remember { mutableStateOf(false) }

    val sorted = remember(images) { images.sortedBy { it.hue() } }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        AnimatedContent(
            targetState = compact,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(250)) },
            label = "modeSwitch"
        ) { isCompact ->
            if (!isCompact) MosaicGrid(sorted) { vm.select(it) }
            else GradientStrip(sorted) { vm.select(it) }
        }

        GlassTopBar(
            title = "Color Mosaic \u25c9",
            onBack = { nav.popBackStack() },
            actions = {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(0.15f))
                        .clickable { compact = !compact },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        if (compact) "\u229e" else "\u2261",
                        color = Primary
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
        )

        if (images.isEmpty()) EmptyState("Grant permission to view photos")
        selected?.let { FullscreenOverlay(it) { vm.select(null) } }
    }
}

@Composable
private fun MosaicGrid(images: List<MediaItem>, onSelect: (MediaItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(3.dp, 72.dp, 3.dp, 16.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(images) { idx, item ->
            val hue = item.hue()
            val tint = Color.hsv(hue, 0.55f, 0.38f)
            val h = (85 + (hue / 360f * 85f).toInt()).dp
            val enter = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(idx * 18L)
                enter.animateTo(1f, tween(300, easing = EaseOutBack))
            }
            val src = remember { MutableInteractionSource() }
            val pressed by src.collectIsPressedAsState()
            val sc by animateFloatAsState(if (pressed) 0.93f else 1f, spring(0.5f), label = "s")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(h)
                    .graphicsLayer {
                        alpha = enter.value
                        translationY = (1f - enter.value) * 35f
                        scaleX = sc; scaleY = sc
                    }
                    .clip(RoundedCornerShape(5.dp))
                    .background(tint)
                    .clickable(interactionSource = src, indication = null) { onSelect(item) }
            ) {
                AsyncImage(
                    model = item.uri, contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize().background(tint.copy(alpha = 0.12f)))
            }
        }
    }
}

@Composable
private fun GradientStrip(images: List<MediaItem>, onSelect: (MediaItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        contentPadding = PaddingValues(2.dp, 72.dp, 2.dp, 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(images) { item ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(3.dp))
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
