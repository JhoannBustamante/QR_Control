package com.espoch.qrcontrol.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.R
import com.espoch.qrcontrol.data.AuthRepository
import com.espoch.qrcontrol.data.BasicValidations
import com.espoch.qrcontrol.ui.theme.qrColors

/**
 * Pantalla de inicio de sesión
 * 
 * Esta pantalla permite a los usuarios autenticarse con email y contraseña.
 * Incluye:
 * - Formulario de login con validaciones
 * - Manejo de estados de carga y errores
 * - Navegación a la pantalla de registro
 * - Integración con Firebase Authentication
 * 
 * @param onRegisterClick Función para navegar a la pantalla de registro
 * @param navegationToHome Función para navegar a la pantalla principal después del login exitoso
 * @param isDarkMode Booleano para determinar si el tema es oscuro
 */
@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit = {},
    navegationToHome: () -> Unit,
    isDarkMode: Boolean
) {
    val colors = qrColors(isDarkMode)
    
    // Estados para manejar el formulario y la UI
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun validateAndLogin() {
        emailError = null
        passwordError = null
        errorMessage = null
        var hasError = false
        if (!BasicValidations.isValidEmail(email)) {
            emailError = "Correo electrónico inválido"
            hasError = true
        }
        if (!BasicValidations.isValidPassword(password)) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            hasError = true
        }
        if (!hasError) {
            isLoading = true
            AuthRepository.login(email, password) { success, error, _ ->
                isLoading = false
                if (success) {
                    navegationToHome()
                } else {
                    errorMessage = error ?: "Error desconocido"
                }
            }
        }
    }

    // Contenedor principal con fondo y centrado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta principal con el formulario de login
        LoginCard(
            email = email,
            onEmailChange = { email = it; emailError = null },
            password = password,
            onPasswordChange = { password = it; passwordError = null },
            emailError = emailError,
            passwordError = passwordError,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onLoginClick = { validateAndLogin() },
            onRegisterClick = onRegisterClick,
            colors = colors
        )
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary, strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
            }
        }
    }
}

/**
 * Tarjeta principal que contiene el formulario de login
 * 
 * @param email Email actual del formulario
 * @param onEmailChange Función para actualizar el email
 * @param password Contraseña actual del formulario
 * @param onPasswordChange Función para actualizar la contraseña
 * @param emailError Mensaje de error para el email
 * @param passwordError Mensaje de error para la contraseña
 * @param errorMessage Mensaje de error a mostrar
 * @param isLoading Estado de carga
 * @param onLoginClick Función para manejar el click en login
 * @param onRegisterClick Función para navegar al registro
 * @param colors Colores del tema actual
 */
@Composable
private fun LoginCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    emailError: String?,
    passwordError: String?,
    errorMessage: String?,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(colors.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Logo de la app
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo QRControl",
                modifier = Modifier.size(100.dp).padding(bottom = 8.dp)
            )
            LoginTitle(colors)
            LoginForm(
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                emailError = emailError,
                passwordError = passwordError,
                colors = colors
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = colors.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
            LoginButton(
                onLoginClick = onLoginClick,
                isLoading = isLoading,
                colors = colors
            )
            RegisterLink(
                onRegisterClick = onRegisterClick,
                colors = colors
            )
        }
    }
}

/**
 * Título de la pantalla de login
 * 
 * @param colors Colores del tema actual
 */
@Composable
private fun LoginTitle(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Text(
        text = "Iniciar sesión",
        style = MaterialTheme.typography.headlineMedium,
        color = colors.text,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

/**
 * Formulario de login con campos de email y contraseña
 * 
 * @param email Email actual
 * @param onEmailChange Función para actualizar el email
 * @param password Contraseña actual
 * @param onPasswordChange Función para actualizar la contraseña
 * @param emailError Mensaje de error para el email
 * @param passwordError Mensaje de error para la contraseña
 * @param colors Colores del tema actual
 */
@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    emailError: String?,
    passwordError: String?,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Correo electrónico", color = colors.text) },
        singleLine = true,
        isError = emailError != null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
    if (emailError != null) {
        Text(
            text = emailError,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña", color = colors.text) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Text(
                    text = if (passwordVisible) "👁️" else "🔒",
                    fontSize = 20.sp
                )
            }
        },
        isError = passwordError != null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
    if (passwordError != null) {
        Text(
            text = passwordError,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
}

/**
 * Botón de login con estado de carga
 * 
 * @param onLoginClick Función para manejar el click
 * @param isLoading Estado de carga
 * @param colors Colores del tema actual
 */
@Composable
private fun LoginButton(
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    Column {
        Button(
            onClick = onLoginClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = "Iniciar sesión",
                color = colors.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Enlace para navegar a la pantalla de registro
 * 
 * @param onRegisterClick Función para navegar al registro
 * @param colors Colores del tema actual
 */
@Composable
private fun RegisterLink(
    onRegisterClick: () -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    Row {
        Text(
            text = "¿No tienes cuenta? ",
            color = colors.text.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Regístrate",
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.clickable { onRegisterClick() }
        )
    }
}

