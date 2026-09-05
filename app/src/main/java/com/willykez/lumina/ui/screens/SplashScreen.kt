package com.willykez.lumina.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberInfiniteTransition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.willykez.lumina.ui.theme.BgDeep
import com.willykez.lumina.ui.theme.Cyan
import com.willykez.lumina.ui.theme.Primary
import com.willykez.lumina.ui.theme.Rose
import com.willykez.lumina.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(nav: NavController) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val ringProgress = remember { Animatable(0f) }

    val inf = rememberInfiniteTransition(label = "splashSpin")
    val spin by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "spin"
    )

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(500)) }
        launch { scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 220f)) }
        launch { ringProgress.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
        delay(1500)
        nav.navigate("home") { popUpTo("splash") { inclusive = true } }
    }

    Box(Modifier.fillMaxSize().background(BgDeep), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .size(200.dp)
                .graphicsLayer { rotationZ = spin }
        ) {
            drawArc(
                brush = Brush.sweepGradient(listOf(Primary, Rose, Cyan, Primary)),
                startAngle = -90f,
                sweepAngle = 360f * ringProgress.value,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(8f, 8f),
                size = androidx.compose.ui.geometry.Size(size.width - 16f, size.height - 16f)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
        ) {
            Text("\u25c8", fontSize = 40.sp, color = Primary)
            Spacer(Modifier.height(10.dp))
            Text(
                "LUMINA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = Brush.linearGradient(listOf(Primary, Rose, Cyan)),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "EXTRAORDINARY GALLERY",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
