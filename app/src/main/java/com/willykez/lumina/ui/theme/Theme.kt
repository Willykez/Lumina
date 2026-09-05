package com.willykez.lumina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Neon,
    background = BgDeep,
    surface = BgSurface,
    surfaceVariant = BgCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Divider
)

@Composable
fun LuminaTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = scheme, typography = LuminaType, content = content)
