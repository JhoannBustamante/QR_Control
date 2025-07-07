package com.espoch.qrcontrol.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog

/**
 * Pantalla de configuración y perfil de usuario.
 * Muestra datos del usuario, modo oscuro y botón de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    username: String = "Usuario",
    role: String = "user",
    email: String = "usuario@espoch.edu.ec",
) {
    val colors = qrColors(isDarkMode)
    var userName by remember { mutableStateOf(username) }
    var userRole by remember { mutableStateOf(role) }
    var userEmail by remember { mutableStateOf(email) }
    var isVerified by remember { mutableStateOf(AuthRepository.isCurrentUserEmailVerified()) }
    var showVerificationSent by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var showPasswordResetSent by remember { mutableStateOf(false) }
    var passwordResetError by remember { mutableStateOf<String?>(null) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var changeEmailSuccess by remember { mutableStateOf(false) }
    var changeEmailError by remember { mutableStateOf<String?>(null) }

    loadUserDataSettings(
        onUserDataLoaded = { userData ->
            userName = userData.name
            userRole = userData.role
            userEmail = userData.email
            isVerified = AuthRepository.isCurrentUserEmailVerified()
            AuthRepository.refreshLocalSession()
        }
    )

    Scaffold(
        containerColor = colors.background,
        content = { padding ->
            SettingsContent(
                onToggleDarkMode = onToggleDarkMode,
                isDarkMode = isDarkMode,
                userName = userName,
                userRole = userRole,
                userEmail = userEmail,
                isVerified = isVerified,
                showVerificationSent = showVerificationSent,
                verificationError = verificationError,
                onSendVerification = {
                    AuthRepository.sendEmailVerification { success, error ->
                        if (success) {
                            showVerificationSent = true
                            verificationError = null
                        } else {
                            verificationError = error
                        }
                    }
                },
                onPasswordReset = {
                    AuthRepository.sendPasswordResetEmail { success, error ->
                        if (success) {
                            showPasswordResetSent = true
                            passwordResetError = null
                        } else {
                            passwordResetError = error
                        }
                    }
                },
                showPasswordResetSent = showPasswordResetSent,
                passwordResetError = passwordResetError,
                showChangeEmailDialog = showChangeEmailDialog,
                onShowChangeEmailDialog = { showChangeEmailDialog = true },
                colors = colors,
                navController = navController,
                padding = padding
            )
        }
    )

    // Modal para cambio de email
    if (showChangeEmailDialog) {
        Dialog(onDismissRequest = { showChangeEmailDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(colors.surface),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cambiar correo electrónico", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showChangeEmailDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.text)
                        }
                    }
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Nuevo correo electrónico", color = colors.text) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            AuthRepository.changeUserEmail(newEmail) { success, error ->
                                if (success) {
                                    changeEmailSuccess = true
                                    changeEmailError = null
                                } else {
                                    changeEmailSuccess = false
                                    changeEmailError = error
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = colors.onSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text("Confirmar cambio", color = colors.onSecondary, fontSize = 15.sp)
                    }
                    if (changeEmailSuccess) {
                        Text("Correo electrónico actualizado correctamente.", color = colors.secondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    if (!changeEmailError.isNullOrEmpty()) {
                        Text(
                            text = changeEmailError ?: "",
                            color = colors.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
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
    onToggleDarkMode: () -> Unit,
    isDarkMode: Boolean,
    userName: String,
    userRole: String,
    userEmail: String,
    isVerified: Boolean,
    showVerificationSent: Boolean,
    verificationError: String?,
    onSendVerification: () -> Unit,
    onPasswordReset: () -> Unit,
    showPasswordResetSent: Boolean,
    passwordResetError: String?,
    showChangeEmailDialog: Boolean,
    onShowChangeEmailDialog: () -> Unit,
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SECCIÓN PREFERENCIAS ---
        Text("Preferencias", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 8.dp))
        DarkModeSwitch(onToggleDarkMode, isDarkMode, colors)
        Spacer(Modifier.height(8.dp))
        // --- SECCIÓN CUENTA ---
        Text("Cuenta", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        Button(
            onClick = { navController.navigate("profile_screen") },
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Perfil",
                tint = colors.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver perfil", color = colors.onPrimary, fontWeight = FontWeight.Medium)
        }
        Card(
            colors = CardDefaults.cardColors(colors.surface),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Estado de verificación
                if (isVerified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = colors.text,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Cuenta verificada", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "No verificado",
                            tint = colors.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Cuenta no verificada", color = colors.error, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onSendVerification,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = colors.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar correo de verificación", color = colors.onPrimary, fontSize = 15.sp)
                    }
                    if (showVerificationSent) {
                        Text("Correo de verificación enviado. Revisa tu bandeja de entrada.", color = colors.primary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    if (verificationError != null) {
                        Text(verificationError, color = colors.error, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onPasswordReset,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = colors.onSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Restablecer contraseña", color = colors.onSecondary, fontSize = 15.sp)
                }
                if (showPasswordResetSent) {
                    Text("Correo de restablecimiento enviado. Revisa tu bandeja de entrada.", color = colors.secondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
                if (passwordResetError != null) {
                    Text(passwordResetError, color = colors.error, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onShowChangeEmailDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = colors.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Cambiar dirección de correo electrónico", color = colors.onPrimary, fontSize = 15.sp)
                }
            }
        }

        // --- SECCIÓN INFORMACIÓN DE LA APP ---
        Spacer(Modifier.height(16.dp))
        Text("Información", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 8.dp))
        AppInfoCard(colors)
        Spacer(modifier = Modifier.weight(1f))
        // --- SECCIÓN CERRAR SESIÓN ---
        LogoutButton(navController, colors)
    }
}

@Composable
private fun DarkModeSwitch(onToggleDarkMode: () -> Unit, isDarkMode: Boolean, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDarkMode) "Modo Oscuro" else "Modo Claro",
            color = colors.text
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
private fun AppInfoCard(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = colors.text,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("QRControl v1.0", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Sistema de Control de Parqueaderos", color = colors.text, fontSize = 15.sp)
                Text("Desarrollado para ESPOCH", color = colors.text, fontSize = 15.sp)
            }
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
            .height(52.dp)
            .padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ExitToApp,
            contentDescription = "Cerrar sesión",
            tint = colors.onPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "CERRAR SESIÓN",
            fontWeight = FontWeight.Bold,
            color = colors.onPrimary,
            fontSize = 16.sp
        )
    }
}

