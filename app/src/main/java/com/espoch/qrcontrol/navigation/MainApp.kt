package com.espoch.qrcontrol.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.espoch.qrcontrol.ui.components.BottomNavBar
import com.espoch.qrcontrol.ui.home.HomeScreen
import com.espoch.qrcontrol.ui.parking.ParkingScreen
import com.espoch.qrcontrol.ui.settings.SettingsScreen
import com.espoch.qrcontrol.ui.Qr.QRRegisteredCarsScreen
import com.espoch.qrcontrol.ui.history.HistoryScreen
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.data.AuthRepository
import java.net.URLDecoder

/**
 * Aplicación principal con navegación inferior
 * 
 * Esta función maneja la navegación principal de la aplicación después del login,
 * incluyendo:
 * - Barra de navegación inferior con Home, Parking y Settings
 * - Navegación entre pantallas principales
 * - Gestión del estado del vehículo seleccionado para parking
 * 
 * @param navController Controlador de navegación principal
 * @param isDarkMode Estado del tema oscuro/claro
 * @param onToggleDarkMode Función para cambiar el tema
 */
@Composable
fun MainApp(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    // Controlador de navegación para la barra inferior
    val bottomNavController = rememberNavController()
    
    // Obtiene el rol del usuario para mostrar elementos específicos
    val userRole = remember { AuthRepository.getLocalUserRole() }
    
    // Lista de elementos de la barra de navegación (diferente para supervisores)
    val items = if (userRole == "supervisor") {
        listOf(Screens.Home, Screens.Parking, Screens.History, Screens.Settings)
    } else {
        listOf(Screens.Home, Screens.Parking, Screens.Settings)
    }
    
    // Estado para el vehículo seleccionado para asignar parking
    var carToAssignParking by remember { mutableStateOf<Cars?>(null) }

    // Scaffold proporciona la estructura básica con barra inferior
    Scaffold(
        bottomBar = {
            BottomNavBar(
                isDarkMode = isDarkMode,
                navController = bottomNavController,
                items = items
            )
        }
    ) { innerPadding ->
        // Navegación principal con las pantallas accesibles desde la barra inferior
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = bottomNavController,
            startDestination = Screens.Home
        ) {
            // Pantalla principal - Muestra información del usuario y opciones principales
            composable(Screens.Home) {
                HomeScreen(
                    isDarkMode = isDarkMode,
                    navController = navController
                )
            }
            
            // Pantalla de gestión de estacionamiento
            composable(Screens.Parking) {
                ParkingScreen(
                    isDarkMode = isDarkMode, 
                    carToAssign = carToAssignParking
                ) { 
                    carToAssignParking = null 
                }
            }
            
            // Pantalla de configuraciones
            composable(Screens.Settings) {
                SettingsScreen(
                    navController = navController,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    username = "Usuario",
                    role = "user",
                    email = "sdfd")
            }
            
            // Pantalla de historial de estacionamiento (solo para supervisores)
            composable(Screens.History) {
                HistoryScreen(
                    isDarkMode = isDarkMode,
                    userRole = userRole
                )
            }
            
            // Pantalla que muestra vehículos registrados para un QR específico
            composable("qr_registered_cars_screen/{qrContent}") { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("qrContent") ?: ""
                val qrContent = URLDecoder.decode(encoded, "UTF-8")
                QRRegisteredCarsScreen(
                    navController = navController,
                    onDismissRequest = { navController.popBackStack() },
                    isDarkMode = isDarkMode,
                    qrContent = qrContent,
                    onParkingAssignRequested = { car ->
                        // Asigna el vehículo seleccionado y navega a parking
                        carToAssignParking = car
                        navController.navigate(Screens.Parking)
                    }
                )
            }
        }
    }
}
