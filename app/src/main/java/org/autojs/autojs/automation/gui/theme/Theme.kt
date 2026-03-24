package org.autojs.autojs.ui.modern.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import org.autojs.autojs.theme.ThemeColorManagerCompat

/**
 * AutoJs6 主题系统
 */
@Composable
fun AutoJsTheme(
    themeColor: Color = ThemeColorManagerCompat.getColorPrimary().toColor(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = rememberColorScheme(darkTheme, themeColor)
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun rememberColorScheme(darkTheme: Boolean, themeColor: Color): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = themeColor,
            secondary = themeColor.copy(alpha = 0.7f),
            tertiary = themeColor.copy(alpha = 0.5f),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFFE0E0E0),
            onSurface = Color(0xFFE0E0E0),
            outline = Color(0xFF49454F),
            outlineVariant = Color(0xFF49454F).copy(alpha = 0.5f),
            scrim = Color(0xFF000000),
        )
    } else {
        lightColorScheme(
            primary = themeColor,
            secondary = themeColor.copy(alpha = 0.7f),
            tertiary = themeColor.copy(alpha = 0.5f),
            background = Color.White,
            surface = Color(0xFFF5F5F5),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0),
            scrim = Color(0xFF000000),
        )
    }
}

// 扩展函数：Int转Color
private fun Int.toColor(): Color = Color(this)
