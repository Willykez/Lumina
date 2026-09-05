package com.willykez.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.willykez.lumina.ui.screens.ClusterScreen
import com.willykez.lumina.ui.screens.ColorMosaicScreen
import com.willykez.lumina.ui.screens.CubeCarouselScreen
import com.willykez.lumina.ui.screens.HomeScreen
import com.willykez.lumina.ui.screens.InfiniteCanvasScreen
import com.willykez.lumina.ui.screens.LayeredPanelsScreen
import com.willykez.lumina.ui.screens.MorphingGalleryScreen
import com.willykez.lumina.ui.screens.ParallaxGalleryScreen
import com.willykez.lumina.ui.screens.SplashScreen
import com.willykez.lumina.ui.theme.LuminaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuminaTheme {
                LuminaApp()
            }
        }
    }
}

/**
 * Registers a destination with a cinematic slide + fade transition:
 * forward navigation slides in from the right while the previous
 * screen recedes slightly to the left, and back navigation reverses
 * the motion — giving every mode switch a sense of physical depth
 * instead of a flat cut.
 */
private fun NavGraphBuilder.animatedScreen(
    route: String,
    content: @Composable () -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(420, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(420))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(320))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(420, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(420))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(320))
        }
    ) { content() }
}

@Composable
fun LuminaApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "splash") {
        composable(
            "splash",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) { SplashScreen(nav) }

        animatedScreen("home") { HomeScreen(nav) }
        animatedScreen("parallax") { ParallaxGalleryScreen(nav) }
        animatedScreen("cube") { CubeCarouselScreen(nav) }
        animatedScreen("morphing") { MorphingGalleryScreen(nav) }
        animatedScreen("canvas") { InfiniteCanvasScreen(nav) }
        animatedScreen("layers") { LayeredPanelsScreen(nav) }
        animatedScreen("mosaic") { ColorMosaicScreen(nav) }
        animatedScreen("cluster") { ClusterScreen(nav) }
    }
}
