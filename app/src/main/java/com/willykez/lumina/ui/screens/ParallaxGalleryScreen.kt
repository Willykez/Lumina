package com.willykez.lumina.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.willykez.lumina.ui.components.EmptyState
import com.willykez.lumina.ui.components.FullscreenOverlay
import com.willykez.lumina.ui.components.GlassTopBar
import com.willykez.lumina.ui.components.NetImage
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.Rose
import com.willykez.lumina.ui.viewmodel.GalleryViewModel
import com.willykez.lumina.utils.RequestMediaPermission

@Composable
fun ParallaxGalleryScreen(nav: NavController, vm: GalleryViewModel = viewModel()) {
    RequestMediaPermission { vm.loadMedia() }
    val images by vm.images.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    val context = LocalContext.current
    var tilt by remember { mutableStateOf(Offset.Zero) }
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                tilt = Offset(-e.values[0] / 9.8f * 18f, -e.values[1] / 9.8f * 10f)
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (sensor != null) sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sm.unregisterListener(listener) }
    }

    val firstIdx by remember { derivedStateOf { gridState.firstVisibleItemIndex } }
    val firstOff by remember { derivedStateOf { gridState.firstVisibleItemScrollOffset.toFloat() } }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            contentPadding = PaddingValues(3.dp, 72.dp, 3.dp, 16.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            itemsIndexed(images) { idx, item ->
                val layer = idx % 3
                val depthFactor = layer * 0.18f
                val row = idx / 3
                val scrollParallax = (firstOff + (row - firstIdx) * 8f) * depthFactor
                val gyroX = tilt.x * (layer + 1) * 0.4f
                val gyroY = tilt.y * (layer + 1) * 0.3f
                val h = when (layer) { 0 -> 132.dp; 1 -> 122.dp; else -> 110.dp }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(h)
                        .graphicsLayer {
                            translationY = -scrollParallax + gyroY
                            translationX = gyroX
                            val d = 1f - layer * 0.04f
                            scaleX = d; scaleY = d
                            shadowElevation = layer * 5f
                        }
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { vm.select(item) }
                ) {
                    NetImage(
                        model = item.uri,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (layer > 0) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = layer * 0.14f))
                        )
                    }
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when (layer) {
                                    0 -> Cyan
                                    1 -> Primary
                                    else -> Rose
                                }.copy(0.7f)
                            )
                    )
                }
            }
        }

        if (images.isEmpty()) EmptyState("Grant permission to view photos")
        GlassTopBar("Parallax Depth \u2726", onBack = { nav.popBackStack() })
        selected?.let { FullscreenOverlay(it) { vm.select(null) } }
    }
}
