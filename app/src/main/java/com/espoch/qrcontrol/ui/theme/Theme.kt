package com.espoch.qrcontrol.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.ColorScheme

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
private fun customColorScheme(isDark: Boolean): ColorScheme {
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
            scrim = colors.outline
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
            scrim = colors.outline
        )
    }
}