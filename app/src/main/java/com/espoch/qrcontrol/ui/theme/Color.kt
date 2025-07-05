package com.espoch.qrcontrol.ui.theme

import androidx.compose.ui.graphics.Color

// Colores por defecto de Material Design (no utilizados actualmente)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFF2A2D2E)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Paleta de colores personalizada para QRControl - Aplicación de Control de Estacionamiento
 * 
 * Esta paleta utiliza colores que transmiten:
 * - Azul profesional: Confianza y estabilidad
 * - Verde de éxito: Operaciones exitosas y disponibilidad
 * - Naranja de advertencia: Estados de ocupación y alertas
 * - Grises modernos: Interfaz limpia y profesional
 */

// --- MODO CLARO ---
val QrPrimaryLight      = Color(0xFF1976D2) // Azul profesional principal
val QrPrimaryDarkText   = Color(0xFFFFFFFF) // Texto blanco sobre azul
val QrPrimaryContainerL = Color(0xFFE3F2FD) // Azul muy claro para contenedores
val QrSecondaryLight    = Color(0xFFFF9800) // Naranja para estados de ocupación
val QrSecondaryDarkText = Color(0xFF000000) // Texto negro sobre naranja
val QrSecondaryContainerL = Color(0xFFFFE0B2) // Naranja claro para contenedores
val QrAccentLight       = Color(0xFF4CAF50) // Verde de éxito/disponibilidad
val QrAccentContainerL  = Color(0xFFC8E6C9) // Verde claro para contenedores
val QrErrorLight        = Color(0xFFF44336) // Rojo de error/ocupado
val QrErrorContainerL   = Color(0xFFFFCDD2) // Rojo claro para contenedores
val QrBackgroundLight   = Color(0xFFFAFAFA) // Gris muy claro para fondo
val QrSurfaceLight      = Color(0xFFFFFFFF) // Blanco para superficies
val QrOutlineLight      = Color(0xFFE0E0E0) // Gris claro para bordes
val QrTextLight         = Color(0xFF212121) // Gris oscuro para texto

// --- MODO OSCURO ---
val QrPrimaryDark       = Color(0xFF64B5F6) // Azul claro para modo oscuro
val QrPrimaryLightText  = Color(0xFF0D47A1) // Azul oscuro para texto sobre azul claro
val QrPrimaryContainerD = Color(0xFF1E3A5F) // Azul oscuro para contenedores
val QrSecondaryDark     = Color(0xFFFFB74D) // Naranja claro para modo oscuro
val QrSecondaryLightText= Color(0xFFE65100) // Naranja oscuro para texto
val QrSecondaryContainerD = Color(0xFF3E2723) // Marrón oscuro para contenedores
val QrAccentDark        = Color(0xFF81C784) // Verde claro para modo oscuro
val QrAccentContainerD  = Color(0xFF1B5E20) // Verde oscuro para contenedores
val QrErrorDark         = Color(0xFFE57373) // Rojo claro para modo oscuro
val QrErrorContainerD   = Color(0xFF4A1C1C) // Rojo oscuro para contenedores
val QrBackgroundDark    = Color(0xFF121212) // Gris muy oscuro para fondo
val QrSurfaceDark       = Color(0xFF1E1E1E) // Gris oscuro para superficies
val QrOutlineDark       = Color(0xFF424242) // Gris medio para bordes
val QrTextDark          = Color(0xFFE0E0E0) // Gris claro para texto
