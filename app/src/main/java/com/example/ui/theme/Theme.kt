package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PharmaTealLight,
    onPrimary = Color(0xFF00382E),
    primaryContainer = PharmaTealDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = PharmaCyanAccent,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = PharmaCobalt,
    onTertiary = Color.White,
    background = PharmaBgDark,
    onBackground = PharmaTextPrimaryDark,
    surface = PharmaSurfaceDark,
    onSurface = PharmaTextPrimaryDark,
    surfaceVariant = PharmaSurfaceVariantDark,
    onSurfaceVariant = PharmaTextSecondaryDark,
    error = PharmaEmergencyRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PharmaTealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = PharmaCobalt,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = PharmaCyanAccent,
    onTertiary = Color.White,
    background = PharmaBgLight,
    onBackground = PharmaTextPrimaryLight,
    surface = PharmaSurfaceLight,
    onSurface = PharmaTextPrimaryLight,
    surfaceVariant = PharmaSurfaceVariantLight,
    onSurfaceVariant = PharmaTextSecondaryLight,
    error = PharmaEmergencyRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional pharma theme colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
