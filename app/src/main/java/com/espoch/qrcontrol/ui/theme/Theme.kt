package com.espoch.qrcontrol.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

/**
 * Esquema de colores para el tema oscuro (no utilizado actualmente)
 * 
 * Define los colores por defecto para el modo oscuro de Material Design 3
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Esquema de colores para el tema claro (no utilizado actualmente)
 * 
 * Define los colores por defecto para el modo claro de Material Design 3
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Tema principal de la aplicación QRControl
 * 
 * Este tema personalizado utiliza la paleta de colores definida en ColorUtils.kt
 * y se adapta automáticamente entre modo claro y oscuro.
 * 
 * Características:
 * - Paleta de colores personalizada para la marca QRControl
 * - Soporte para modo claro y oscuro
 * - Colores dinámicos desactivados para mantener consistencia
 * - Tipografía personalizada
 * 
 * @param darkTheme Si es true, aplica el tema oscuro; si es false, el tema claro
 * @param dynamicColor Desactivado para usar solo la paleta personalizada
 * @param content Contenido de la aplicación que usará este tema
 */
@Composable
fun QRControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para usar solo la paleta personalizada
    content: @Composable () -> Unit
) {
    val colorScheme = customColorScheme(darkTheme)
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
}

/**
 * Genera el esquema de colores personalizado según el modo claro/oscuro
 * 
 * Utiliza la función qrColors() de ColorUtils.kt para obtener la paleta
 * apropiada y la mapea a los componentes de Material Design 3.
 * 
 * @param isDark Si es true, usa colores del modo oscuro; si es false, del modo claro
 * @return ColorScheme configurado con la paleta personalizada
 */
@Composable
private fun customColorScheme(isDark: Boolean): androidx.compose.material3.ColorScheme {
    val colors = qrColors(isDark)
    return if (isDark) {
        // Esquema de colores para modo oscuro
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.primary,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.secondary,
            tertiary = colors.accent,
            onTertiary = colors.onPrimary,
            tertiaryContainer = colors.accentContainer,
            onTertiaryContainer = colors.accent,
            error = colors.error,
            onError = colors.onPrimary,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.error,
            background = colors.background,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            surfaceVariant = colors.surface,
            onSurfaceVariant = colors.text,
            outline = colors.outline,
            inverseOnSurface = colors.background,
            inverseSurface = colors.text,
            inversePrimary = colors.primary,
            surfaceTint = colors.primary,
            scrim = Color.Black
        )
    } else {
        // Esquema de colores para modo claro
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.primary,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.secondary,
            tertiary = colors.accent,
            onTertiary = colors.onPrimary,
            tertiaryContainer = colors.accentContainer,
            onTertiaryContainer = colors.accent,
            error = colors.error,
            onError = colors.onPrimary,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.error,
            background = colors.background,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            surfaceVariant = colors.surface,
            onSurfaceVariant = colors.text,
            outline = colors.outline,
            inverseOnSurface = colors.background,
            inverseSurface = colors.text,
            inversePrimary = colors.primary,
            surfaceTint = colors.primary,
            scrim = Color.Black
        )
    }
}