package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = Color(0xFF04202C),
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = Color(0xFF70F5FF),
    secondary = NovaViolet,
    onSecondary = Color(0xFF1E0A40),
    secondaryContainer = Color(0xFF381A6E),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = NovaMagenta,
    background = DeepSpaceBackground,
    onBackground = TextPrimaryDark,
    surface = DeepSpaceSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DeepSpaceSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF374151)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark assistant theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> NovaDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
