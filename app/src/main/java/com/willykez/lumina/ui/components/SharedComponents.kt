package com.willykez.lumina.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberInfiniteTransition
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.willykez.lumina.data.MediaItem
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.TextMuted
import com.willykez.lumina.ui.theme.TextPrimary
import kotlinx.coroutines.launch

val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/**
 * Animated shimmer sweep used as a loading placeholder — a soft diagonal
 * band of light drifting across a dark surface, looping indefinitely.
 */
@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "shimmer")
    val sweep by inf.animateFloat(
        -1f, 2f,
        infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "sweep"
    )
    Box(
        modifier
            .background(Color(0xFF1B1B2E))
            .graphicsLayer { clip = true }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.07f),
                            Color.Transparent
                        ),
                        start = Offset(sweep * 300f - 150f, 0f),
                        end = Offset(sweep * 300f + 150f, 300f)
                    )
                )
        )
    }
}

/**
 * Drop-in replacement for AsyncImage that shows an animated shimmer
 * placeholder until the image has actually decoded, then crossfades in.
 */
@Composable
fun NetImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var loaded by remember(model) { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(
        targetValue = if (loaded) 1f else 0f,
        animationSpec = tween(260),
        label = "imageFade"
    )
    Box(modifier) {
        if (!loaded) ShimmerPlaceholder(Modifier.fillMaxSize())
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onSuccess = { loaded = true },
            onError = { loaded = true },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = fadeAlpha }
        )
    }
}

@Composable
fun GlassTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val inf = rememberInfiniteTransition(label = "underline")
    val glow by inf.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(BgDeep.copy(alpha = 0.97f), Color.Transparent))
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
            Row(content = actions)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Primary.copy(alpha = glow),
                            Cyan.copy(alpha = glow * 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun FullscreenOverlay(item: MediaItem, onDismiss: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val scaleAnim = remember { Animatable(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val txState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scaleAnim.value * zoomChange).coerceIn(1f, 6f)
        scope.launch { scaleAnim.snapTo(newScale) }
        offset = if (newScale > 1f) offset + panChange else Offset.Zero
    }

    BackHandler {
        if (scaleAnim.value > 1.02f) {
            scope.launch { scaleAnim.animateTo(1f, tween(220)) }
            offset = Offset.Zero
        } else {
            onDismiss()
        }
    }

    // Bouncy spring entrance rather than a flat fade
    val enterScale = remember { Animatable(0.86f) }
    val enterAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { enterAlpha.animateTo(1f, tween(220)) }
        launch { enterScale.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 420f)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(enterAlpha.value)
            .background(Color.Black.copy(alpha = 0.94f))
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .transformable(txState)
                .pointerInput(item.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                if (scaleAnim.value > 1.05f) {
                                    scaleAnim.animateTo(1f, tween(260))
                                    offset = Offset.Zero
                                } else {
                                    scaleAnim.animateTo(2.6f, tween(260))
                                }
                            }
                        },
                        onTap = {
                            if (scaleAnim.value <= 1.05f) onDismiss()
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scaleAnim.value * enterScale.value
                    scaleY = scaleAnim.value * enterScale.value
                    translationX = offset.x
                    translationY = offset.y
                }
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.75f)))
                )
                .navigationBarsPadding()
                .padding(16.dp, 32.dp, 16.dp, 16.dp)
        ) {
            Column {
                Text(
                    item.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${item.width}\u00d7${item.height}  \u2022  ${item.size / 1024} KB  \u2022  double-tap to zoom",
                    color = Color.White.copy(0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    val inf = rememberInfiniteTransition(label = "float")
    val floatY by inf.animateFloat(
        -6f, 6f,
        infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatY"
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "\u25ce",
                fontSize = 48.sp,
                color = Primary,
                modifier = Modifier.graphicsLayer { translationY = floatY }
            )
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
