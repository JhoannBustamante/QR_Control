package com.espoch.qrcontrol.ui.components


import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.espoch.qrcontrol.R
import com.espoch.qrcontrol.navigation.Screens
import com.espoch.qrcontrol.ui.theme.qrColors

/**
 * Barra de navegación inferior de la aplicación
 * 
 * Esta componente proporciona navegación entre las pantallas principales:
 * - Home: Pantalla principal con información del usuario
 * - Parking: Gestión de estacionamiento
 * - Settings: Configuraciones de la aplicación
 * 
 * Características:
 * - Navegación con estado persistente
 * - Iconos y etiquetas para cada pantalla
 * - Indicador visual de la pantalla actual
 * - Soporte para tema oscuro/claro
 * 
 * @param isDarkMode Estado del tema oscuro/claro
 * @param navController Controlador de navegación
 * @param items Lista de pantallas disponibles en la barra
 */
@Composable
fun BottomNavBar(
    isDarkMode: Boolean,
    navController: NavHostController,
    items: List<String>
) {
    val colors = qrColors(isDarkMode)
    
    // Obtiene la ruta actual para determinar qué elemento está seleccionado
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = colors.background,
    ) {
        // Crea un elemento de navegación para cada pantalla
        items.forEach { screen ->
            val selected = currentRoute == screen
            
            // Mapea cada pantalla a su icono y etiqueta correspondiente
            val (iconRes, label) = when (screen) {
                Screens.Home -> R.drawable.ic_home to "Inicio"
                Screens.Parking -> R.drawable.ic_parking to "Parqueadero"
                Screens.History -> R.drawable.ic_history to "Historial"
                Screens.Settings -> R.drawable.ic_settings to "Settings"
                else -> R.drawable.ic_home to screen
            }

            NavigationBarItem(
                icon = { Icon(painter = painterResource(id = iconRes), contentDescription = label) },
                label = { Text(label, color = colors.text)  },
                selected = selected,
                onClick = {
                    // Solo navega si no estamos ya en esa pantalla
                    if (currentRoute != screen) {
                        navController.navigate(screen) {
                            // Configuración para mantener el estado de navegación
                            popUpTo(Screens.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}