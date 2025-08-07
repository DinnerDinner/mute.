package com.example.mute_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WellnessLightScheme = lightColorScheme(
    primary = Color(0xFFFF9AA2),
    secondary = Color(0xFF87CEEB),
    tertiary = Color(0xFF98FB98),
    background = Color(0xFFF8F6F0),
    surface = Color(0xFFFFE5E5),
    surfaceVariant = Color(0xFFE8F4F8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF2D2D2D),
    onBackground = Color(0xFF2D2D2D),
    onSurface = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFF4A4A4A),
)

private val WellnessDarkScheme = darkColorScheme(
    primary = Color(0xFFE68C3A),
    secondary = Color(0xFF9AA085),
    tertiary = Color(0xFF8B4B5C),
    background = Color(0xFF1F1E1A),
    surface = Color(0xFF2A2926),
    surfaceVariant = Color(0xFF3A3935),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE8E3D3),
    onSurface = Color(0xFFE8E3D3),
    onSurfaceVariant = Color(0xFFC8C3B3),
)

@Composable
fun MuteAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WellnessDarkScheme
        else -> WellnessLightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
