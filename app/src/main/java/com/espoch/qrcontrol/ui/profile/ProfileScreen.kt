package com.espoch.qrcontrol.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.espoch.qrcontrol.ui.theme.qrColors
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.espoch.qrcontrol.data.AuthRepository
import com.espoch.qrcontrol.model.user
import com.espoch.qrcontrol.ui.theme.QrColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.espoch.qrcontrol.R
import android.widget.Toast
import androidx.compose.foundation.clickable
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.tasks.await
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de perfil del usuario
 * 
 * Muestra información detallada del usuario incluyendo:
 * - Foto de perfil (avatar)
 * - Información personal (nombre, email, rol)
 * - Estadísticas del usuario
 * - Opciones de configuración
 * 
 * @param navController Controlador de navegación
 * @param isDarkMode Estado del tema oscuro/claro
 * @param onBackClick Función para volver atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    onBackClick: () -> Unit
) {
    val colors = qrColors(isDarkMode)
    var userData by remember { mutableStateOf<user?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        AuthRepository.getUserData { user ->
            userData = user
            isLoading = false
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        color = colors.text,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.primary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con avatar y datos principales
                ProfileHeader(userData, colors, onEditClick = { showEditDialog = true })
                // Botón Editar perfil (ahora solo visual, el diálogo se abre con onEditClick)
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = colors.text, thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))
                // Información detallada
                UserInfoCard(userData, colors)
            }
        }
        // Diálogo de edición de perfil
        if (showEditDialog) {
            EditProfileDialog(
                userData = userData,
                onSave = { newName ->
                    if (userData == null) return@EditProfileDialog
                    isUploading = true
                    uploadError = null
                    val userId = userData!!.id
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(userId)
                    coroutineScope.launch {
                        try {
                            val updates = mutableMapOf<String, Any>("name" to newName)
                            userRef.update(updates).await()
                            isUploading = false
                            showEditDialog = false
                            // Refrescar datos
                            AuthRepository.getUserData { user ->
                                userData = user
                            }
                        } catch (e: Exception) {
                            isUploading = false
                            uploadError = "Error al guardar: ${e.message}"
                        }
                    }
                },
                onDismiss = {
                    showEditDialog = false
                },
                isUploading = isUploading,
                uploadError = uploadError,
                context = context
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProfileHeader(userData: user?, colors: QrColors, onEditClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Solo icono de usuario por defecto en el avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.1f))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = colors.text,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Nombre del usuario
                Text(
                    text = userData?.name ?: "Usuario",
                    color = colors.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                // Email del usuario
                Text(
                    text = userData?.email ?: "usuario@espoch.edu.ec",
                    color = colors.text,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                // Rol del usuario como chip
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (userData?.role == "supervisor") colors.primaryContainer else colors.secondaryContainer,
                    shape = RoundedCornerShape(50),
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = if (userData?.role == "supervisor") "Supervisor" else "Usuario",
                        color = if (userData?.role == "supervisor") colors.text else colors.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserInfoCard(userData: user?, colors: QrColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Información Personal",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                icon = painterResource(id = R.drawable.ic_person),
                label = "Nombre",
                value = userData?.name ?: "No disponible",
                colors = colors
            )
            InfoRow(
                icon = painterResource(id = R.drawable.ic_email),
                label = "Email",
                value = userData?.email ?: "No disponible",
                colors = colors
            )
            InfoRow(
                icon = painterResource(id = R.drawable.ic_security),
                label = "Rol",
                value = if (userData?.role == "supervisor") "Supervisor" else "Usuario",
                colors = colors
            )
            InfoRow(
                icon = painterResource(id = R.drawable.ic_date_range),
                label = "Miembro desde",
                value = formatTimestamp(userData?.createdAt ?: ""),
                colors = colors
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: Painter,
    label: String,
    value: String,
    colors: QrColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            tint = colors.text,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = colors.text,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    userData: user?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    isUploading: Boolean,
    uploadError: String?,
    context: android.content.Context
) {
    var newName by remember { mutableStateOf(userData?.name ?: "") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Imagen de perfil
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        nameError = false
                    },
                    label = { Text("Nombre") },
                    isError = nameError,
                    singleLine = true
                )
                if (nameError) {
                    Text("El nombre no puede estar vacío", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = userData?.email ?: "",
                    onValueChange = { /* No editable */ },
                    label = { Text("Email") },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Para cambiar el email ve a Configuración > Cuenta", Toast.LENGTH_LONG).show()
                    }
                )
                if (uploadError != null) {
                    Text(uploadError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isBlank()) {
                        nameError = true
                    } else {
                        onSave(newName)
                    }
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Función para formatear el timestamp a fecha legible
fun formatTimestamp(timestamp: String): String {
    return try {
        val date = Date(timestamp.toLong())
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        format.format(date)
    } catch (e: Exception) {
        "$e No disponible"
    }
} 