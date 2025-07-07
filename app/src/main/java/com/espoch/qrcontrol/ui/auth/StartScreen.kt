package com.espoch.qrcontrol.ui.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.espoch.qrcontrol.R
import com.espoch.qrcontrol.data.AuthRepository
import com.espoch.qrcontrol.ui.Custom.GeneralButton
import com.espoch.qrcontrol.ui.theme.QrColors
import com.espoch.qrcontrol.ui.theme.qrColors
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn

/**
 * Pantalla de bienvenida y punto de entrada de la aplicación
 * 
 * Esta pantalla permite al usuario:
 * - Iniciar sesión con email/contraseña
 * - Iniciar sesión con Google
 * - Navegar a la pantalla de registro
 * 
 * @param onLoginClick Función para navegar a la pantalla de login
 * @param onLoginSuccessGoogle Función llamada cuando el login con Google es exitoso
 */
@Composable
fun StartScreen(
    onLoginClick: () -> Unit = {},
    onLoginSuccessGoogle: () -> Unit,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val colors = qrColors(isDarkMode)
    
    // Verifica si hay una sesión válida al cargar la pantalla
    LaunchedEffect(Unit) {
        if (AuthRepository.hasValidSession()) {
            onLoginSuccessGoogle()
        }
    }
    
    var isLoadingGoogle by remember { mutableStateOf(false) }
    // Configuración del launcher para Google Sign-In
    val launcher = rememberGoogleSignInLauncher(context, onLoginSuccessGoogle, colors, onLoading = { isLoadingGoogle = it })
    val googleSignInClient = createGoogleSignInClient(context)

    // Contenedor principal con fondo y centrado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta de bienvenida con logo y botones de autenticación
        WelcomeCard(
            onLoginClick = onLoginClick,
            onGoogleSignIn = {
                isLoadingGoogle = true
                launcher.launch(googleSignInClient.signInIntent)
            },
            colors = colors
        )
        // Spinner de carga modal
        if (isLoadingGoogle) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(colors.surface, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
        }
    }
}

/**
 * Crea el cliente de Google Sign-In con las opciones necesarias
 * 
 * @param context Contexto de la aplicación
 * @return Cliente configurado para Google Sign-In
 */
private fun createGoogleSignInClient(context: android.content.Context): com.google.android.gms.auth.api.signin.GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, gso)
}

/**
 * Tarjeta de bienvenida que contiene el logo, título y botones de autenticación
 * 
 * @param onLoginClick Función para navegar al login con email
 * @param onGoogleSignIn Función para iniciar el proceso de login con Google
 * @param colors Colores del tema actual
 */
@Composable
private fun WelcomeCard(
    onLoginClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            AppLogo()
            WelcomeTitle(colors)
            LoginButtons(
                onLoginClick = onLoginClick,
                onGoogleSignIn = onGoogleSignIn,
                colors = colors
            )
        }
    }
}

/**
 * Logo de la aplicación
 */
@Composable
private fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = "App Logo",
        modifier = Modifier.size(164.dp)
    )
}

/**
 * Título de bienvenida de la aplicación
 * 
 * @param colors Colores del tema actual
 */
@Composable
private fun WelcomeTitle(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Text(
        text = "Bienvenido a \n QR Control",
        color = colors.text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineMedium,
    )
}

/**
 * Botones de autenticación (Email y Google)
 * 
 * @param onLoginClick Función para login con email
 * @param onGoogleSignIn Función para login con Google
 * @param colors Colores del tema actual
 */
@Composable
private fun LoginButtons(
    onLoginClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    colors: QrColors
) {
    // Botón para login con email
    GeneralButton(
        Modifier.clickable { onLoginClick() },
        painterResource(id = R.drawable.ic_email),
        "Continuar con Email",
        colors
    )
    
    // Botón para login con Google
    GeneralButton(
        Modifier.clickable { onGoogleSignIn() },
        painterResource(id = R.drawable.ic_google_logo),
        "Continuar con Google",
        colors
    )
}

/**
 * Configura el launcher para Google Sign-In
 * 
 * @param context Contexto de la aplicación
 * @param onLoginSuccessGoogle Función llamada cuando el login es exitoso
 * @param colors Colores del tema actual
 * @param onLoading Función para manejar el estado de carga
 * @return Launcher configurado para Google Sign-In
 */
@Composable
private fun rememberGoogleSignInLauncher(
    context: android.content.Context,
    onLoginSuccessGoogle: () -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    onLoading: (Boolean) -> Unit
): androidx.activity.result.ActivityResultLauncher<android.content.Intent> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleGoogleSignInResult(result, context, onLoginSuccessGoogle, onLoading)
    }
}

/**
 * Maneja el resultado del proceso de Google Sign-In
 * 
 * @param result Resultado de la actividad de Google Sign-In
 * @param context Contexto de la aplicación
 * @param onLoginSuccessGoogle Función llamada cuando el login es exitoso
 * @param onLoading Función para manejar el estado de carga
 */
private fun handleGoogleSignInResult(
    result: androidx.activity.result.ActivityResult,
    context: android.content.Context,
    onLoginSuccessGoogle: () -> Unit,
    onLoading: (Boolean) -> Unit
) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.result
        val idToken = account.idToken
        if (idToken != null) {
            AuthRepository.AuthWithGoogle(idToken) { success, error, role ->
                onLoading(false)
                if (success) {
                    onLoginSuccessGoogle()
                } else {
                    Toast.makeText(context, error ?: "Error con Google", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            onLoading(false)
        }
    } catch (e: Exception) {
        onLoading(false)
        Toast.makeText(context, "Fallo Google Sign-In", Toast.LENGTH_SHORT).show()
    }
}

