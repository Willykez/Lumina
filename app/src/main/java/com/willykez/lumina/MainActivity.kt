package com.willykez.lumina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.willykez.lumina.ui.screens.CubeCarouselScreen
import com.willykez.lumina.ui.screens.ClusterScreen
import com.willykez.lumina.ui.screens.ColorMosaicScreen
import com.willykez.lumina.ui.screens.HomeScreen
import com.willykez.lumina.ui.screens.InfiniteCanvasScreen
import com.willykez.lumina.ui.screens.LayeredPanelsScreen
import com.willykez.lumina.ui.screens.MorphingGalleryScreen
import com.willykez.lumina.ui.screens.ParallaxGalleryScreen
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

@Composable
fun LuminaApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(nav) }
        composable("parallax") { ParallaxGalleryScreen(nav) }
        composable("cube") { CubeCarouselScreen(nav) }
        composable("morphing") { MorphingGalleryScreen(nav) }
        composable("canvas") { InfiniteCanvasScreen(nav) }
        composable("layers") { LayeredPanelsScreen(nav) }
        composable("mosaic") { ColorMosaicScreen(nav) }
        composable("cluster") { ClusterScreen(nav) }
    }
}
