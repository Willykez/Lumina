package com.willykez.lumina.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission

@Composable
fun MorphingGalleryScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val images by vm.images.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                if (targetState == null)
                    (fadeIn(tween(300)) + scaleIn(tween(300), 0.88f)) togetherWith
                        (fadeOut(tween(200)) + scaleOut(tween(200), 1.05f))
                else
                    (fadeIn(tween(300)) + scaleIn(tween(300), 1.05f)) togetherWith
                        (fadeOut(tween(200)) + scaleOut(tween(200), 0.92f))
            },
            label = "morph"
        ) { sel ->
            if (sel == null) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(108.dp),
                    contentPadding = PaddingValues(4.dp, 72.dp, 4.dp, 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(images) { idx, item ->
                        MorphThumbnail(item, idx) { vm.select(item) }
                    }
                }
            } else {
                FullscreenOverlay(sel) { vm.select(null) }
            }
        }

        if (images.isEmpty()) EmptyState("Grant permission to view photos")
        GlassTopBar("Morphing Grid \u2b21", onBack = { nav.popBackStack() })
    }
}

@Composable
private fun MorphThumbnail(item: MediaItem, idx: Int, onClick: () -> Unit) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(idx * 25L)
        enter.animateTo(1f, tween(360, easing = EaseOutBack))
    }
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(0.55f, 500f), label = "s")
    val h = (92 + idx * 41 % 88).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(h)
            .graphicsLayer {
                scaleX = scale * enter.value
                scaleY = scale * enter.value
                alpha = enter.value
                translationY = (1f - enter.value) * 50f
                clip = true
                shape = RoundedCornerShape(8.dp)
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable(interactionSource = src, indication = null) { onClick() }
    ) {
        AsyncImage(
            model = item.uri, contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        AnimatedVisibility(pressed, enter = fadeIn(tween(80)), exit = fadeOut(tween(80))) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(Color.White.copy(0.18f), Color.Transparent)))
            )
        }
    }
}
