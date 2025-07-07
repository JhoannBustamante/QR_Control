package com.espoch.qrcontrol.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.R
import com.espoch.qrcontrol.ui.theme.qrColors
import androidx.compose.ui.text.font.FontWeight
import com.espoch.qrcontrol.ui.Qr.AddNewCarScreen
import com.espoch.qrcontrol.data.AuthRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.Calendar
import androidx.navigation.NavHostController
import com.espoch.qrcontrol.data.ParkingRepository
import com.espoch.qrcontrol.model.Cars
import androidx.compose.foundation.lazy.LazyColumn
import com.espoch.qrcontrol.model.user
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import com.espoch.qrcontrol.ui.Qr.QRCodeViewScreen
import com.espoch.qrcontrol.ui.theme.QrColors
import kotlinx.coroutines.launch
import coil3.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

/**
 * Pantalla principal de la aplicación
 * 
 * Esta es la pantalla central que se muestra después del login.
 * Características principales:
 * - Saludo personalizado según la hora del día
 * - Información del usuario (nombre, rol)
 * - Lista de vehículos registrados (para usuarios normales)
 * - Botón flotante para escanear QR (solo supervisores)
 * - Opción para agregar nuevos vehículos
 * 
 * La pantalla se adapta según el rol del usuario:
 * - Usuario normal: Ve sus vehículos y puede agregar nuevos
 * - Supervisor: Ve todos los vehículos que estan en el parqueadero  y puede escanear QR
 * 
 * @param navController Controlador de navegación
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    isDarkMode: Boolean
) {
    val colors = qrColors(isDarkMode)
    
    // Estados para manejar la UI y datos del usuario
    val showAddCarDialog = remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Usuario") }
    var userRole by remember { mutableStateOf("user") }
    var userId by remember { mutableStateOf("") }
    var userCars by remember { mutableStateOf<List<Cars>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carga los datos del usuario autenticado
    LoadUserData(
        onUserDataLoaded = { userData ->
            userName = userData.name
            userRole = userData.role
            userId = userData.id
            if (userData.role == "user") {
                isLoading = true
                loadUserCars(userData.id) { cars ->
                    userCars = cars
                    isLoading = false
                }
            } else {
                isLoading = false
            }
            AuthRepository.refreshLocalSession()
        }
    )

    // Saludo personalizado según la hora
    val greeting = remember { getGreetingByHour() }

    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            // Botón flotante solo para supervisores
            SupervisorFAB(
                userRole = userRole,
                onScanClick = { navController.navigate("qr_scanner_screen") },
                colors = colors
            )
        }
    ) { innerPadding ->
        // Contenido principal de la pantalla
        HomeContent(
            colors = colors,
            greeting = greeting,
            userName = userName,
            userRole = userRole,
            userCars = userCars,
            onAddCarClick = { showAddCarDialog.value = true },
            onCarUpdated = {
                if (userRole == "user" && userId.isNotEmpty()) {
                    isLoading = true
                    loadUserCars(userId) { cars ->
                        userCars = cars
                        isLoading = false
                    }
                }
            },
            innerPadding = innerPadding,
            isLoading = isLoading,
            navController = navController
        )
        
        // Diálogo para agregar nuevo vehículo
        if (showAddCarDialog.value) {
            AddNewCarScreen(
                colors = colors,
                onDismissRequest = { showAddCarDialog.value = false },
                onCarAdded = {
                    // Recarga los vehículos después de agregar uno nuevo
                    if (userRole == "user" && userId.isNotEmpty()) {
                        isLoading = true
                        loadUserCars(userId) { cars ->
                            userCars = cars
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}

/**
 * Carga los datos del usuario autenticado
 * 
 * @param onUserDataLoaded Callback con los datos del usuario cargados
 */
@Composable
private fun LoadUserData(onUserDataLoaded: (user) -> Unit) {
    LaunchedEffect(Unit) {
        AuthRepository.getUserData { userData ->
            userData?.let { onUserDataLoaded(it) }
        }
    }
}

/**
 * Carga los vehículos de un usuario específico
 * 
 * @param ownerId ID del propietario de los vehículos
 * @param onCarsLoaded Callback con la lista de vehículos cargados
 */
private fun loadUserCars(ownerId: String, onCarsLoaded: (List<Cars>) -> Unit) {
    ParkingRepository.getCarsByOwnerId(ownerId, onCarsLoaded)
}

/**
 * Genera un saludo personalizado según la hora del día
 * 
 * @return Saludo apropiado para la hora actual
 */
private fun getGreetingByHour(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

/**
 * Botón flotante para supervisores
 * 
 * Solo se muestra para usuarios con rol "supervisor"
 * Permite escanear códigos QR para gestionar estacionamiento
 * 
 * @param userRole Rol del usuario actual
 * @param onScanClick Función para manejar el click en escanear
 * @param colors Colores del tema actual
 */
@Composable
private fun SupervisorFAB(
    userRole: String,
    onScanClick: () -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    if (userRole == "supervisor") {
        FloatingActionButton(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            onClick = onScanClick,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qr_scan),
                contentDescription = "Escanear QR",
                tint = colors.onPrimary
            )
        }
    }
}

/**
 * Contenido principal de la pantalla de inicio
 * 
 * Organiza todos los elementos de la UI en una lista desplazable
 * 
 * @param colors Colores del tema actual
 * @param greeting Saludo personalizado
 * @param userName Nombre del usuario
 * @param userRole Rol del usuario
 * @param userCars Lista de vehículos del usuario
 * @param onAddCarClick Función para agregar vehículo
 * @param onCarUpdated Función cuando se actualiza un vehículo
 * @param innerPadding Padding interno del Scaffold
 */
@Composable
private fun HomeContent(
    colors: QrColors,
    greeting: String,
    userName: String,
    userRole: String,
    userCars: List<Cars>,
    onAddCarClick: () -> Unit,
    onCarUpdated: () -> Unit,
    innerPadding: PaddingValues,
    isLoading: Boolean,
    navController: NavHostController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(innerPadding)
            .padding(16.dp, 5.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Tarjeta de saludo
        item {
            GreetingCard(colors, greeting, userName) {
                navController.navigate("profile_screen")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Contenido específico para usuarios normales
        if (userRole == "user") {
            item {
                AddCarButton(colors, onAddCarClick)
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                if (isLoading) {
                    // Skeletons mientras carga
                    RegisteredCarsSkeleton(colors)
                } else {
                RegisteredCarsCard(
                    colors = colors,
                    userCars = userCars,
                    onCarUpdated = onCarUpdated
                )
                }
            }
        }
        
        // Contenido específico para supervisores
        if (userRole == "supervisor") {
            item {
                SupervisorRegisteredCarsCard(
                    colors = colors
                )
            }
        }
    }
}

/**
 * Tarjeta que muestra todos los vehículos registrados (vista de supervisor)
 * 
 * @param colors Colores del tema actual
 */
@Composable
private fun SupervisorRegisteredCarsCard(
    colors: QrColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SupervisorCarsSection(
                colors = colors
            )
        }
    }
}

@Composable
private fun SupervisorCarsSection(
    colors: QrColors
) {
    Text(
        text = "Autos en el Parqueadero",
        color = colors.text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )
    
    var allCars by remember { mutableStateOf<List<Cars>>(emptyList()) }
    LaunchedEffect(Unit) {
        ParkingRepository.getAllCars { cars ->
            allCars = cars.filter { it.parkingId != 0 }
        }
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        allCars.forEach { car ->
            CarsScreenSupervisor(car = car, colors = colors)
        }
    }
}

@Composable
private fun GreetingCard(colors: QrColors, greeting: String, userName: String, onProfileClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
    ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.1f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Ver perfil",
                    tint = colors.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
            Text(
                    text = "$greeting,",
                color = colors.text,
                fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
            )
                Text(
                    text = userName,
                    color = colors.text,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AddCarButton(colors: QrColors, onAddCarClick: () -> Unit) {
    Button(
        onClick = onAddCarClick,
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Agregar auto",
            tint = colors.onPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Agregar nuevo auto", color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun RegisteredCarsCard(
    colors: QrColors,
    userCars: List<Cars>,
    onCarUpdated: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tus autos registrados",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            if (userCars.isEmpty()) {
                Text(
                    text = "No tienes autos registrados.",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
            } else {
                userCars.forEach { car ->
                    CarItemCardWithActions(car, colors, onCarUpdated)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private enum class DialogType { NONE, QR, EDIT, DELETE }

@Composable
private fun CarItemCardWithActions(car: Cars, colors: QrColors, onCarUpdated: () -> Unit) {
    var dialogType by remember { mutableStateOf(DialogType.NONE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.accentContainer),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Auto",
                    tint = colors.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text("${car.name} - ${car.plate}", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Marca: ${car.brand}", color = colors.text.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text("Color: ${car.color}", color = colors.text.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                ActionButton(text = "QR", colors = colors) { dialogType = DialogType.QR }
                ActionButton(text = "Editar", colors = colors) { dialogType = DialogType.EDIT }
                ActionButton(text = "Eliminar", colors = colors) { dialogType = DialogType.DELETE }
            }
        }
    }
    when (dialogType) {
        DialogType.QR -> QRCodeViewScreen(
            colors = colors,
            onDismissRequest = { dialogType = DialogType.NONE },
            car = car,
            onCancel = { dialogType = DialogType.NONE }
        )
        DialogType.EDIT -> AddNewCarScreen(
            colors = colors,
            onDismissRequest = { dialogType = DialogType.NONE },
            onCarAdded = {
                dialogType = DialogType.NONE
                onCarUpdated()
            },
            carToEdit = car,
            isEdit = true,
            customTitle = "Editar información del auto"
        )
        DialogType.DELETE -> ConfirmDeleteDialog(
            car = car,
            colors = colors,
            onConfirm = {
                ParkingRepository.deleteCar(car.plate) { success, error ->
                    scope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("Auto eliminado correctamente")
                            dialogType = DialogType.NONE
                            onCarUpdated()
                        } else {
                            snackbarHostState.showSnackbar("Error: $error")
            }
        }
    }
            },
            onCancel = { dialogType = DialogType.NONE }
        )
        else -> {}
    }
    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun ActionButton(text: String, colors: QrColors, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
        modifier = Modifier.height(40.dp).padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            color = colors.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    car: Cars,
    colors: QrColors,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Eliminar auto") },
        text = { Text("¿Estás seguro de que deseas eliminar el auto con placa ${car.plate}?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = colors.error)) {
                Text("Eliminar", color = colors.onPrimary)
            }
        },
        dismissButton = {
            Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)) {
                Text("Cancelar", color = colors.onSecondary)
            }
        }
    )
}

@Composable
private fun RegisteredCarsSkeleton(colors: QrColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
                    .background(colors.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            )
            Spacer(Modifier.height(16.dp))
            repeat(2) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(colors.primary.copy(alpha = 0.15f), CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(16.dp)
                                .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(12.dp)
                                .background(colors.primary.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
            }
        }
    }
}

