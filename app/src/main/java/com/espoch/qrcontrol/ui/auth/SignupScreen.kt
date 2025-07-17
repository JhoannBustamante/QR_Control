package com.espoch.qrcontrol.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.data.AuthRepository
import com.espoch.qrcontrol.data.BasicValidations
import com.espoch.qrcontrol.ui.theme.qrColors
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import com.espoch.qrcontrol.R
import com.espoch.qrcontrol.ui.theme.QrColors


/**
 * Pantalla de registro de nuevos usuarios
 * 
 * Esta pantalla permite a los usuarios crear una nueva cuenta en el sistema.
 * Incluye:
 * - Formulario de registro con validaciones completas
 * - Campos: nombre, email, contraseña y confirmación
 * - Manejo de estados de carga y errores
 * - Navegación a la pantalla de login
 * - Integración con Firebase Authentication
 * 
 * @param onLoginClick Función para navegar a la pantalla de login
 */
@Composable
fun SignupScreen(
    onLoginClick: () -> Unit = {},
    isDarkMode: Boolean
) {
    val colors = qrColors(isDarkMode)
    
    // Estados para manejar el formulario y la UI
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun validateAndSignup() {
        nameError = null
        emailError = null
        passwordError = null
        confirmError = null
        errorMessage = null
        var hasError = false
        if (!BasicValidations.isValidUsername(name)) {
            nameError = "El nombre no puede estar vacío"
            hasError = true
        }
        if (!BasicValidations.isValidEmail(email)) {
            emailError = "Correo electrónico inválido"
            hasError = true
        }
        if (!BasicValidations.isValidPassword(password)) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            hasError = true
        }
        if (password != confirm) {
            confirmError = "Las contraseñas no coinciden"
            hasError = true
        }
        if (!hasError) {
            isLoading = true
            AuthRepository.signup(email, password, name) { success, error, _ ->
                isLoading = false
                if (success) {
                    onLoginClick()
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
        // Tarjeta principal con el formulario de registro
        SignupCard(
            name = name,
            onNameChange = { name = it; nameError = null },
            email = email,
            onEmailChange = { email = it; emailError = null },
            password = password,
            onPasswordChange = { password = it; passwordError = null },
            confirm = confirm,
            onConfirmChange = { confirm = it; confirmError = null },
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmError = confirmError,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onSignupClick = { validateAndSignup() },
            onLoginClick = onLoginClick,
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
 * Tarjeta principal que contiene el formulario de registro
 * 
 * @param name Nombre actual del formulario
 * @param onNameChange Función para actualizar el nombre
 * @param email Email actual del formulario
 * @param onEmailChange Función para actualizar el email
 * @param password Contraseña actual del formulario
 * @param onPasswordChange Función para actualizar la contraseña
 * @param confirm Confirmación actual de la contraseña
 * @param onConfirmChange Función para actualizar la confirmación
 * @param nameError Mensaje de error para el nombre
 * @param emailError Mensaje de error para el email
 * @param passwordError Mensaje de error para la contraseña
 * @param confirmError Mensaje de error para la confirmación
 * @param errorMessage Mensaje de error a mostrar
 * @param isLoading Estado de carga
 * @param onSignupClick Función para manejar el click en registro
 * @param onLoginClick Función para navegar al login
 * @param colors Colores del tema actual
 */
@Composable
private fun SignupCard(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirm: String,
    onConfirmChange: (String) -> Unit,
    nameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmError: String?,
    errorMessage: String?,
    isLoading: Boolean,
    onSignupClick: () -> Unit,
    onLoginClick: () -> Unit,
    colors: QrColors
) {
    val scrollState = rememberScrollState()
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(colors.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 350.dp, max = 600.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Logo de la app
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo QRControl",
                modifier = Modifier.size(100.dp).padding(bottom = 8.dp)
            )
            SignupTitle(colors)
            SignupForm(
                name = name,
                onNameChange = onNameChange,
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                confirm = confirm,
                onConfirmChange = onConfirmChange,
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmError = confirmError,
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
            SignupButton(
                onSignupClick = onSignupClick,
                isLoading = isLoading,
                colors = colors
            )
            LoginLink(
                onLoginClick = onLoginClick,
                colors = colors
            )
        }
    }
}

/**
 * Título de la pantalla de registro
 * 
 * @param colors Colores del tema actual
 */
@Composable
private fun SignupTitle(colors: QrColors) {
    Text(
        text = "Crea tu cuenta",
        style = MaterialTheme.typography.headlineMedium,
        color = colors.text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

/**
 * Formulario de registro con todos los campos necesarios
 * 
 * @param name Nombre actual
 * @param onNameChange Función para actualizar el nombre
 * @param email Email actual
 * @param onEmailChange Función para actualizar el email
 * @param password Contraseña actual
 * @param onPasswordChange Función para actualizar la contraseña
 * @param confirm Confirmación actual de la contraseña
 * @param onConfirmChange Función para actualizar la confirmación
 * @param nameError Mensaje de error para el nombre
 * @param emailError Mensaje de error para el email
 * @param passwordError Mensaje de error para la contraseña
 * @param confirmError Mensaje de error para la confirmación
 * @param colors Colores del tema actual
 */
@Composable
private fun SignupForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirm: String,
    onConfirmChange: (String) -> Unit,
    nameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmError: String?,
    colors: QrColors
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nombre", color = colors.text) },
        singleLine = true,
        isError = nameError != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
    )
    if (nameError != null) {
        Text(
            text = nameError,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Correo electrónico", color = colors.text) },
        singleLine = true,
        isError = emailError != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
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
    OutlinedTextField(
        value = confirm,
        onValueChange = onConfirmChange,
        label = { Text("Confirmar contraseña", color = colors.text) },
        singleLine = true,
        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                Text(
                    text = if (confirmVisible) "👁️" else "🔒",
                    fontSize = 20.sp
                )
            }
        },
        isError = confirmError != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
    )
    if (confirmError != null) {
        Text(
            text = confirmError,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
}

/**
 * Botón de registro con estado de carga
 * 
 * @param onSignupClick Función para manejar el click
 * @param isLoading Estado de carga
 * @param colors Colores del tema actual
 */
@Composable
private fun SignupButton(
    onSignupClick: () -> Unit,
    isLoading: Boolean,
    colors: QrColors
) {
    Column {
        Button(
            onClick = onSignupClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(colors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = "Regístrate",
                color = colors.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Enlace para navegar a la pantalla de login
 * 
 * @param onLoginClick Función para navegar al login
 * @param colors Colores del tema actual
 */
@Composable
private fun LoginLink(
    onLoginClick: () -> Unit,
    colors: QrColors
) {
    Row {
        Text(
            text = "¿Ya tienes cuenta? ",
            color = colors.text.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Inicia sesión",
            color = colors.text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onLoginClick() }
        )
    }
}

