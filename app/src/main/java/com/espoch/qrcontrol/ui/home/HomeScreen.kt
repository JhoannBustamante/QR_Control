package com.espoch.qrcontrol.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.espoch.qrcontrol.model.user

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
 * @param isDarkMode Estado del tema oscuro/claro
 * @param navController Controlador de navegación
 */
@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    navController: NavHostController
) {
    val colors = qrColors(isDarkMode)
    
    // Estados para manejar la UI y datos del usuario
    val showAddCarDialog = remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Usuario") }
    var userRole by remember { mutableStateOf("user") }
    var userId by remember { mutableStateOf("") }
    var userCars by remember { mutableStateOf<List<Cars>>(emptyList()) }

    // Carga los datos del usuario autenticado
    LoadUserData(
        onUserDataLoaded = { userData ->
            userName = userData.name
            userRole = userData.role
            userId = userData.id
            // Si es usuario normal, carga sus vehículos
            if (userData.role == "user") {
                loadUserCars(userData.id) { cars ->
                    userCars = cars
                }
            }
            // Refresca la sesión local cuando el usuario interactúa con la app
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
            isDarkMode = isDarkMode,
            onAddCarClick = { showAddCarDialog.value = true },
            onCarUpdated = {
                // Recarga los vehículos cuando se actualiza uno
                if (userRole == "user" && userId.isNotEmpty()) {
                    loadUserCars(userId) { cars ->
                        userCars = cars
                    }
                }
            },
            innerPadding = innerPadding
        )
        
        // Diálogo para agregar nuevo vehículo
        if (showAddCarDialog.value) {
            AddNewCarScreen(
                isDarkMode = isDarkMode,
                onDismissRequest = { showAddCarDialog.value = false },
                onCarAdded = {
                    // Recarga los vehículos después de agregar uno nuevo
                    if (userRole == "user" && userId.isNotEmpty()) {
                        loadUserCars(userId) { cars ->
                            userCars = cars
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
 * @param isDarkMode Estado del tema
 * @param onAddCarClick Función para agregar vehículo
 * @param onCarUpdated Función cuando se actualiza un vehículo
 * @param innerPadding Padding interno del Scaffold
 */
@Composable
private fun HomeContent(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    greeting: String,
    userName: String,
    userRole: String,
    userCars: List<Cars>,
    isDarkMode: Boolean,
    onAddCarClick: () -> Unit,
    onCarUpdated: () -> Unit,
    innerPadding: PaddingValues
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
            GreetingCard(colors, greeting, userName)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Contenido específico para usuarios normales
        if (userRole == "user") {
            item {
                AddCarButton(colors, onAddCarClick)
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                RegisteredCarsCard(
                    colors = colors,
                    userCars = userCars,
                    isDarkMode = isDarkMode,
                    onCarUpdated = onCarUpdated
                )
            }
        }
        
        // Contenido específico para supervisores
        if (userRole == "supervisor") {
            item {
                SupervisorRegisteredCarsCard(
                    colors = colors,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

/**
 * Tarjeta que muestra todos los vehículos registrados (vista de supervisor)
 * 
 * @param colors Colores del tema actual
 * @param isDarkMode Estado del tema
 */
@Composable
private fun SupervisorRegisteredCarsCard(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    isDarkMode: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SupervisorCarsSection(
                colors = colors,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun SupervisorCarsSection(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    isDarkMode: Boolean
) {
    Text(
        text = "Autos en el Parqueadero",
        color = colors.primary,
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
            CarsScreenSupervisor(car = car, isDarkMode = isDarkMode)
        }
    }
}

@Composable
private fun GreetingCard(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    greeting: String,
    userName: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$greeting\n$userName",
                color = colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AddCarButton(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary
        )
    ) {
        Text(
            text = "AGREGAR AUTO",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RegisteredCarsCard(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    userCars: List<Cars>,
    isDarkMode: Boolean,
    onCarUpdated: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mis Autos Registrados",
                color = colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            if (userCars.isEmpty()) {
                EmptyCarsMessage(colors)
            } else {
                CarsList(
                    cars = userCars,
                    isDarkMode = isDarkMode,
                    onCarUpdated = onCarUpdated
                )
            }
        }
    }
}

@Composable
private fun EmptyCarsMessage(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay autos registrados",
            color = colors.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun CarsList(
    cars: List<Cars>,
    isDarkMode: Boolean,
    onCarUpdated: (() -> Unit)? = null
) {
    cars.forEach { car ->
        CarsScreenUser(
            car = car,
            isDarkMode = isDarkMode,
            onCarUpdated = onCarUpdated
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

