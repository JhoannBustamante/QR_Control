package com.espoch.qrcontrol.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

/**
 * Genera la paleta de colores personalizada según el modo claro/oscuro
 * 
 * Esta función centraliza la selección de colores para toda la aplicación.
 * Utiliza colores personalizados definidos en Color.kt que siguen la
 * identidad visual de QRControl.
 * 
 * Los colores se organizan siguiendo las convenciones de Material Design 3:
 * - primary: Color principal de la marca
 * - secondary: Color secundario para elementos de apoyo
 * - accent: Color de acento para destacar elementos
 * - error: Color para estados de error
 * - background/surface: Colores de fondo
 * - text: Color del texto principal
 * 
 * @param isDark Si es true, devuelve colores del modo oscuro; si es false, del modo claro
 * @return QrColors con la paleta apropiada para el modo seleccionado
 */
@Composable
fun qrColors(isDark: Boolean): QrColors = if (isDark) {
    // Paleta de colores para modo oscuro
    QrColors(
        primary = QrPrimaryDark,
        onPrimary = QrPrimaryDarkText,
        primaryContainer = QrPrimaryContainerD,
        secondary = QrSecondaryDark,
        onSecondary = QrSecondaryLightText,
        secondaryContainer = QrSecondaryContainerD,
        accent = QrAccentDark,
        accentContainer = QrAccentContainerD,
        error = QrErrorDark,
        errorContainer = QrErrorContainerD,
        background = QrBackgroundDark,
        surface = QrSurfaceDark,
        outline = QrOutlineDark,
        text = QrTextDark
    )
} else {
    // Paleta de colores para modo claro
    QrColors(
        primary = QrPrimaryLight,
        onPrimary = QrPrimaryDarkText,
        primaryContainer = QrPrimaryContainerL,
        secondary = QrSecondaryLight,
        onSecondary = QrSecondaryDarkText,
        secondaryContainer = QrSecondaryContainerL,
        accent = QrAccentLight,
        accentContainer = QrAccentContainerL,
        error = QrErrorLight,
        errorContainer = QrErrorContainerL,
        background = QrBackgroundLight,
        surface = QrSurfaceLight,
        outline = QrOutlineLight,
        text = QrTextLight
    )
}

/**
 * Clase de datos que agrupa todos los colores personalizados de la aplicación
 * 
 * Esta clase define la estructura de colores utilizada en toda la aplicación.
 * Cada propiedad representa un aspecto específico de la interfaz:
 * 
 * - primary: Color principal de la marca QRControl
 * - onPrimary: Color del texto sobre el color primario
 * - primaryContainer: Color de contenedores primarios (tarjetas, botones)
 * - secondary: Color secundario para elementos de apoyo
 * - onSecondary: Color del texto sobre el color secundario
 * - secondaryContainer: Color de contenedores secundarios
 * - accent: Color de acento para destacar elementos importantes
 * - accentContainer: Color de contenedores de acento
 * - error: Color para estados de error y advertencias
 * - errorContainer: Color de contenedores de error
 * - background: Color de fondo principal de la aplicación
 * - surface: Color de superficies (tarjetas, diálogos)
 * - outline: Color de bordes y líneas divisorias
 * - text: Color del texto principal en la aplicación
 */
data class QrColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val accent: Color,
    val accentContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val background: Color,
    val surface: Color,
    val outline: Color,
    val text: Color
)
