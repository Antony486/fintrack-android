package com.fintrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colors
val Primary = Color(0xFF1E6BE6)
val PrimaryDark = Color(0xFF1454B8)
val Secondary = Color(0xFF03DAC6)
val Background = Color(0xFFF5F7FA)
val Surface = Color(0xFFFFFFFF)
val Error = Color(0xFFE53935)

val IncomeGreen = Color(0xFF2E7D32)
val ExpenseRed = Color(0xFFC62828)
val NeutralGray = Color(0xFF757575)
val CardBackground = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFEEEEEE)

// Dark theme
val PrimaryDarkTheme = Color(0xFF5C9EFF)
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    error = Error,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkTheme,
    secondary = Secondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = Error,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun FinTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
