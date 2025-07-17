package com.espoch.qrcontrol.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.espoch.qrcontrol.ui.auth.LoginScreen
import com.espoch.qrcontrol.ui.auth.SignupScreen
import com.espoch.qrcontrol.ui.auth.StartScreen
import com.espoch.qrcontrol.ui.Qr.QRScannerScreen
import com.espoch.qrcontrol.ui.Qr.QRRegisteredCarsScreen
import com.espoch.qrcontrol.ui.history.HistoryScreen
import com.espoch.qrcontrol.ui.profile.ProfileScreen
import com.google.gson.Gson
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.ui.parking.ParkingScreen
import com.espoch.qrcontrol.data.AuthRepository
import java.net.URLEncoder
import java.net.URLDecoder

@SuppressLint("StaticFieldLeak")
private lateinit var navController : NavHostController

/**
 * Definición de todas las pantallas disponibles en la aplicación
 * Cada constante representa una ruta de navegación
 */
object Screens {
    const val Login = "login_screen"           // Pantalla de inicio de sesión
    const val Start = "start_screen"           // Pantalla de bienvenida
    const val Signup = "signup_screen"         // Pantalla de registro
    const val Home = "home_screen"             // Pantalla principal con navegación inferior
    const val Parking = "parking_screen"       // Gestión de estacionamiento
    const val Settings = "settings_screen"     // Configuraciones
    const val History = "history_screen"       // Historial de estacionamiento (solo supervisores)
    const val Profile = "profile_screen"       // Perfil del usuario
}

/**
 * Navegación principal de la aplicación
 * 
 * Define el flujo de navegación:
 * 1. StartScreen (pantalla de bienvenida) → Login/Signup
 * 2. Login/Signup → Home (pantalla principal)
 * 3. Home → QR Scanner → QR Registered Cars → Parking
 * 
 * @param onToggleDarkMode Función para cambiar el tema
 */
@Composable
fun NavGraph(isDarkMode: Boolean, onToggleDarkMode: () -> Unit) {
    val navController = rememberNavController()
    
    // Verifica si hay una sesión válida al iniciar la app
    val hasValidSession = remember { AuthRepository.hasValidSession() }
    
    // Determina el destino inicial basado en la sesión
    val startDestination = if (hasValidSession) Screens.Home else Screens.Start
    
    // Verifica la sesión periódicamente y navega si es necesario
    LaunchedEffect(Unit) {
        if (hasValidSession) {
            // Si hay sesión válida, navega a Home
            navController.navigate(Screens.Home) {
                popUpTo(Screens.Start) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    
    // Configuración del grafo de navegación con destino inicial dinámico
    NavHost(navController, startDestination = startDestination) {
        
        // Pantalla de bienvenida - Punto de entrada de la aplicación
        composable(Screens.Start) {
            StartScreen(
                onLoginClick = { navController.navigate(Screens.Login) { launchSingleTop = true } },
                onLoginSuccessGoogle = {
                    // Navega a Home y limpia el stack de navegación
                    navController.navigate(Screens.Home) {
                        popUpTo(Screens.Start) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla de inicio de sesión
        composable(Screens.Login) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Screens.Signup) { launchSingleTop = true } },
                navegationToHome = {
                    // Navega a Home después del login exitoso
                    navController.navigate(Screens.Home) {
                        popUpTo(Screens.Start) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla de registro
        composable(Screens.Signup) {
            SignupScreen(
                onLoginClick = { navController.navigate(Screens.Login) { launchSingleTop = true } },
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla principal con navegación inferior
        composable (Screens.Home) {
            MainApp(navController, isDarkMode, onToggleDarkMode)
        }
        
        // Escáner de códigos QR
        composable("qr_scanner_screen") {
            QRScannerScreen(
                onQrScanned = { qrContent ->
                    // Codifica el contenido QR para pasarlo como parámetro de navegación
                    val encoded = URLEncoder.encode(qrContent, "UTF-8")
                    navController.navigate("qr_registered_cars_screen/$encoded")
                },
                onDismiss = { navController.popBackStack() },
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla que muestra los vehículos registrados para el QR escaneado
        composable("qr_registered_cars_screen/{qrContent}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("qrContent") ?: ""
            val qrContent = URLDecoder.decode(encoded, "UTF-8")
            QRRegisteredCarsScreen(
                navController = navController,
                onDismissRequest = { navController.popBackStack() },
                qrContent = qrContent,
                onNavigateToParking = { car ->
                    // Serializa el vehículo seleccionado para pasarlo a la pantalla de parking
                    val carJson = URLEncoder.encode(Gson().toJson(car), "UTF-8")
                    navController.navigate("parking_screen/$carJson")
                },
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla de gestión de estacionamiento
        composable("parking_screen/{carJson}") { backStackEntry ->
            val carJson = backStackEntry.arguments?.getString("carJson") ?: ""
            val car = Gson().fromJson(carJson, Cars::class.java)
            ParkingScreen(
                carToAssign = car,
                onParkingAssigned = { navController.navigate(Screens.Home) },
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla de historial de estacionamiento (solo para supervisores)
        composable(Screens.History) {
            HistoryScreen(
                userRole = AuthRepository.getLocalUserRole(),
                isDarkMode = isDarkMode
            )
        }
        
        // Pantalla de perfil del usuario
        composable(Screens.Profile) {
            ProfileScreen(
                navController = navController,
                isDarkMode = isDarkMode,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

