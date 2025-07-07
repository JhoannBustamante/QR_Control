package com.espoch.qrcontrol.ui.Qr

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.espoch.qrcontrol.data.ParkingRepository
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.ui.theme.qrColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.navigation.NavController
import com.espoch.qrcontrol.ui.theme.QrColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.LocalTextStyle

@Composable
fun QRRegisteredCarsScreen(
    navController: NavController,
    onDismissRequest: () -> Unit,
    qrContent: String,
    onParkingAssignRequested: ((car: Cars) -> Unit)? = null,
    onNavigateToParking: ((car: Cars) -> Unit)? = null,
    isDarkMode: Boolean
) {
    val colors = qrColors(isDarkMode)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val carFromQR = remember(qrContent) { parseCarFromQR(qrContent) }
    var carFromDB by remember { mutableStateOf<Cars?>(null) }

    // Si hay placa, consulta la base de datos para obtener el auto completo
    LaunchedEffect(carFromQR?.plate) {
        carFromQR?.plate?.takeIf { it.isNotBlank() }?.let { plate ->
            ParkingRepository.getCarByPlate(plate) { car ->
                if (car != null) carFromDB = car
            }
        }
    }

    var formState by remember {
        mutableStateOf(
            (carFromDB ?: carFromQR)?.let { createFormStateFromCar(it) } ?: QRCarFormState()
        )
    }

    // Estado para mostrar el Dialog de selección de espacio
    var showParkingDialog by remember { mutableStateOf(false) }
    var parkingSpots by remember { mutableStateOf(listOf<com.espoch.qrcontrol.model.ParkingSpot>()) }
    var selectedSpotId by remember { mutableStateOf<Int?>(null) }
    var isLoadingSpots by remember { mutableStateOf(false) }

    // Función para cargar los espacios disponibles
    fun loadAvailableSpots() {
        isLoadingSpots = true
        ParkingRepository.getParkingSpots { spots ->
            parkingSpots = spots.filter { it.estado == "disponible" }.sortedBy { it.id }
            isLoadingSpots = false
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        CarRegistrationCard(
            formState = formState,
            onFormStateChange = { formState = it },
            onSave = {
                loadAvailableSpots()
                showParkingDialog = true
            },
            onCancel = onDismissRequest,
            colors = colors,
            car = (carFromDB ?: carFromQR) ?: Cars()
        )
    }

    // AlertDialog para seleccionar espacio disponible
    if (showParkingDialog) {
        AlertDialog(
            onDismissRequest = { showParkingDialog = false },
            title = { Text("Selecciona un espacio disponible") },
            text = {
                if (isLoadingSpots) {
                    CircularProgressIndicator()
                } else if (parkingSpots.isEmpty()) {
                    Text("No hay espacios disponibles.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(parkingSpots) { spot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSpotId == spot.id,
                                    onClick = { selectedSpotId = spot.id }
                                )
                                Text("Espacio #${spot.id}", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showParkingDialog = false
                        selectedSpotId?.let { spotId ->
                            handleParkingAssignment(
                                formState = formState,
                                spotId = spotId,
                                scope = scope,
                                snackbarHostState = snackbarHostState,
                                onSuccess = {
                                    navController.navigate("home_screen") {
                                        popUpTo("home_screen") { inclusive = true }
                                    }
                                    onParkingAssignRequested?.invoke(formState.toCar((carFromDB ?: carFromQR) ?: Cars()))
                                }
                            )
                        }
                    },
                    enabled = selectedSpotId != null && !isLoadingSpots
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showParkingDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun createFormStateFromCar(car: Cars): QRCarFormState {
    return QRCarFormState(
        name = car.name,
        plate = car.plate,
        brand = car.brand,
        model = car.model,
        color = car.color,
        owner = car.ownerName,
        ownerId = car.ownerId,
        parkingId = car.parkingId
    )
}

private fun handleCarSave(
    formState: QRCarFormState,
    carFromQR: Cars?,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onParkingAssignRequested: ((car: Cars) -> Unit)?,
    onNavigateToParking: ((car: Cars) -> Unit)?
) {
    val car = formState.toCar(carFromQR)
    scope.launch {
        ParkingRepository.addCar(car) { success, error ->
            scope.launch {
                if (success) {
                    snackbarHostState.showSnackbar("Auto guardado correctamente")
                    onParkingAssignRequested?.invoke(car)
                    onNavigateToParking?.invoke(car)
                } else {
                    snackbarHostState.showSnackbar("Error: $error")
                }
            }
        }
    }
}

private fun parseCarFromQR(qrContent: String): Cars? = try {
    val obj = JSONObject(qrContent)
    Cars(
        id = obj.optInt("id"),
        name = obj.optString("name"),
        plate = obj.optString("plate"),
        brand = obj.optString("brand"),
        model = obj.optString("model"),
        color = obj.optString("color"),
        ownerName = obj.optString("ownerName"),
        ownerId = obj.optString("ownerId"),
        parkingId = obj.optInt("parkingId")
    )
} catch (_: Exception) { null }

private data class QRCarFormState(
    val name: String = "",
    val plate: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val owner: String = "",
    val ownerId: String = "",
    val parkingId: Int = 0
) {
    fun toCar(original: Cars?): Cars = Cars(
        id = original?.id ?: 0,
        name = name,
        plate = plate,
        brand = brand,
        model = model,
        color = color,
        ownerName = owner,
        ownerId = ownerId,
        parkingId = parkingId
    )
}

@Composable
private fun CarRegistrationCard(
    formState: QRCarFormState,
    onFormStateChange: (QRCarFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    colors: QrColors,
    car: Cars
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Card(
        colors = CardDefaults.cardColors(colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RegistrationTitle(colors)
            CarFormFields(
                formState = formState,
                onValueChange = onFormStateChange,
                colors = colors
            )
            if (errorMessage != null) {
                Text(errorMessage!!, color = colors.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            FormButtons(
                colors = colors,
                onSave = {
                    // Validación de campos obligatorios
                    if (formState.name.isBlank() || formState.plate.isBlank() || formState.brand.isBlank() || formState.model.isBlank() || formState.color.isBlank()) {
                        errorMessage = "Completa todos los campos obligatorios."
                    } else {
                        errorMessage = null
                        onSave()
                    }
                },
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun RegistrationTitle(colors: QrColors) {
    Text(
        text = "Registrar Ingreso de Auto",
        color = colors.primary,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun CarFormFields(
    formState: QRCarFormState,
    onValueChange: (QRCarFormState) -> Unit,
    colors: QrColors
) {
    Column {
        FormField(
            value = formState.name,
            onValueChange = { onValueChange(formState.copy(name = it)) },
            label = "Nombre del Auto",
            colors = colors
        )
        FormField(
            value = formState.plate,
            onValueChange = { onValueChange(formState.copy(plate = it)) },
            label = "Placa",
            colors = colors
        )
        FormField(
            value = formState.brand,
            onValueChange = { onValueChange(formState.copy(brand = it)) },
            label = "Marca",
            colors = colors
        )
        FormField(
            value = formState.model,
            onValueChange = { onValueChange(formState.copy(model = it)) },
            label = "Modelo",
            colors = colors
        )
        FormField(
            value = formState.color,
            onValueChange = { onValueChange(formState.copy(color = it)) },
            label = "Color",
            colors = colors
        )
        FormField(
            value = formState.owner,
            onValueChange = { onValueChange(formState.copy(owner = it)) },
            label = "Propietario",
            colors = colors
        )
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    colors: QrColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colors.text) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = colors.text),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun FormButtons(
    colors: QrColors,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ActionButton(
            text = "Guardar",
            onClick = onSave,
            colors = colors.primary,
            textColor = colors.onPrimary,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Cancelar",
            onClick = onCancel,
            colors = colors.secondary,
            textColor = colors.onSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    colors: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colors),
        modifier = modifier
            .height(48.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 18.sp
        )
    }
}

private fun handleParkingAssignment(
    formState: QRCarFormState,
    spotId: Int,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSuccess: () -> Unit
) {
    val car = formState.toCar(null)
    val plate = car.plate
    val userId = car.ownerId
    val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    val historial = com.espoch.qrcontrol.model.HistorialParking(
        id = (System.currentTimeMillis() / 1000).toInt(), // id único basado en timestamp
        userId = userId,
        carId = plate,
        parkingSpotId = spotId.toString(),
        entryDate = now,
        exitDate = ""
    )
    scope.launch {
        // 1. Actualizar el estado del espacio
        ParkingRepository.updateParkingSpot(
            spotId, "ocupado", plate
        ) { success, error ->
            if (success) {
                // 2. Guardar historial
                ParkingRepository.createHistorialParking(historial) { histSuccess, histError ->
                    if (histSuccess) {
                        // 3. Actualizar parkingId del auto
                        ParkingRepository.updateCarParkingId(plate, spotId) { carSuccess, carError ->
                            if (carSuccess) {
                                scope.launch { snackbarHostState.showSnackbar("Registro exitoso") }
                                onSuccess()
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Error al actualizar auto: $carError") }
                            }
                        }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Error al guardar historial: $histError") }
                    }
                }
            } else {
                scope.launch { snackbarHostState.showSnackbar("Error al actualizar espacio: $error") }
            }
        }
    }
}
