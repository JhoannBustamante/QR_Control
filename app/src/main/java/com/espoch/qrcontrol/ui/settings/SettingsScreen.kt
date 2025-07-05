package com.espoch.qrcontrol.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.ui.theme.qrColors
import androidx.navigation.NavHostController
import com.espoch.qrcontrol.data.AuthRepository
import com.espoch.qrcontrol.data.SessionManager
import com.espoch.qrcontrol.navigation.Screens
import com.espoch.qrcontrol.model.user

/**
 * Pantalla de configuración y perfil de usuario.
 * Muestra datos del usuario, modo oscuro y botón de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    username: String = "Usuario",
    role: String = "user",
    email: String = "usuario@espoch.edu.ec",
) {
    val colors = qrColors(isDarkMode)
    var userName by remember { mutableStateOf(username) }
    var userRole by remember { mutableStateOf(role) }
    var userEmail by remember { mutableStateOf(email) }

    loadUserDataSettings(
        onUserDataLoaded = { userData ->
            userName = userData.name
            userRole = userData.role
            userEmail = userData.email
            // Refresca la sesión local cuando el usuario accede a configuración
            AuthRepository.refreshLocalSession()
        }
    )

    Scaffold(
        containerColor = colors.background,
        content = { padding ->
            SettingsContent(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                userName = userName,
                userRole = userRole,
                userEmail = userEmail,
                colors = colors,
                navController = navController,
                padding = padding
            )
        }
    )
}

@Composable
private fun loadUserDataSettings(onUserDataLoaded: (user) -> Unit) {
    LaunchedEffect(Unit) {
        AuthRepository.getUserData { userData ->
            userData?.let { onUserDataLoaded(it) }
        }
    }
}

@Composable
private fun SettingsContent(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    userName: String,
    userRole: String,
    userEmail: String,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    navController: NavHostController,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(padding)
            .padding(16.dp, 5.dp),
        horizontalAlignment = Alignment.Start
    ) {
        DarkModeSwitch(isDarkMode, onToggleDarkMode, colors)
        Spacer(Modifier.height(16.dp))
        UserInfoCard(userName, userRole, userEmail, colors)
        Spacer(Modifier.height(16.dp))
        AppInfoCard(colors)
        Spacer(modifier = Modifier.weight(1f))
        LogoutButton(navController, colors)
    }
}

@Composable
private fun DarkModeSwitch(isDarkMode: Boolean, onToggleDarkMode: () -> Unit, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDarkMode) "Modo Oscuro" else "Modo Claro",
            color = colors.primary
        )
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = isDarkMode,
            onCheckedChange = { 
                onToggleDarkMode()
                // Guarda la preferencia del tema inmediatamente
                SessionManager.saveDarkModePreference(!isDarkMode)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                uncheckedThumbColor = colors.primary,
                checkedTrackColor = colors.secondary,
                uncheckedTrackColor = colors.secondaryContainer,
            )
        )
    }
}

@Composable
private fun UserInfoCard(username: String, role: String, email: String, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Información de Cuenta",
                color = colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text("Usuario: $username", color = colors.primary, fontSize = 15.sp)
            Text("Rol: $role", color = colors.primary, fontSize = 15.sp)
            Text("Email: $email", color = colors.primary, fontSize = 15.sp)
        }
    }
}

@Composable
private fun AppInfoCard(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Información de la App",
                color = colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text("QRControl v1.0", color = colors.primary, fontSize = 15.sp)
            Text("Sistema de Control de Parqueaderos", color = colors.primary, fontSize = 15.sp)
            Text("Desarrollado para ESPOCH", color = colors.primary, fontSize = 15.sp)
        }
    }
}

@Composable
private fun LogoutButton(navController: NavHostController, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Button(
        onClick = {
            AuthRepository.logout()
            navController.navigate(Screens.Start) {
                popUpTo(Screens.Home) { inclusive = true }
                launchSingleTop = true
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = colors.error),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = "CERRAR SESIÓN",
            fontWeight = FontWeight.Bold,
            color = colors.onPrimary,
            fontSize = 16.sp
        )
    }
}

