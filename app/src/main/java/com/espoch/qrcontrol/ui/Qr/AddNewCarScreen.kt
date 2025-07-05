package com.espoch.qrcontrol.ui.Qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.ui.theme.qrColors
import com.espoch.qrcontrol.data.ParkingRepository
import com.espoch.qrcontrol.data.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.text.style.TextAlign

// Data class para el estado del formulario
private data class CarFormState(
    val name: String = "",
    val plate: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = ""
)

// Data class para los errores de validación por campo
private data class CarFormErrors(
    val name: String? = null,
    val plate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val color: String? = null
)

@Composable
fun AddNewCarScreen(
    isDarkMode: Boolean,
    onDismissRequest: () -> Unit,
    onCarAdded: (() -> Unit)? = null,
    carToEdit: Cars? = null,
    isEdit: Boolean = false,
    customTitle: String? = null
) {
    val colors = qrColors(isDarkMode)
    var formState by remember {
        mutableStateOf(
            if (carToEdit != null) CarFormState(
                name = carToEdit.name,
                plate = carToEdit.plate,
                brand = carToEdit.brand,
                model = carToEdit.model,
                color = carToEdit.color
            ) else CarFormState()
        )
    }
    var formErrors by remember { mutableStateOf(CarFormErrors()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun saveCar() {
        val errors = validateCarFormFields(formState)
        if (errors != CarFormErrors()) {
            formErrors = errors
            return
        }
        // Si es edición, verificar si hubo cambios
        if (isEdit && carToEdit != null) {
            val noChanges = formState.name == carToEdit.name &&
                formState.plate == carToEdit.plate &&
                formState.brand == carToEdit.brand &&
                formState.model == carToEdit.model &&
                formState.color == carToEdit.color
            if (noChanges) {
                scope.launch {
                    snackbarHostState.showSnackbar("No existen cambios para actualizar en el registro.")
                }
                return
            }
        }
        formErrors = CarFormErrors() // Limpiar errores si todo está bien
        handleSaveCar(
            formState = formState,
            carToEdit = carToEdit,
            isEdit = isEdit,
            onDismissRequest = onDismissRequest,
            onCarAdded = onCarAdded,
            snackbarHostState = snackbarHostState,
            scope = scope
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        AddCarCard(
            formState = formState,
            onFormStateChange = { formState = it; formErrors = CarFormErrors() },
            onSave = { saveCar() },
            onCancel = onDismissRequest,
            colors = colors,
            isEdit = isEdit,
            customTitle = customTitle,
            snackbarHostState = snackbarHostState,
            formErrors = formErrors
        )
    }
}

private fun handleSaveCar(
    formState: CarFormState,
    carToEdit: Cars?,
    isEdit: Boolean,
    onDismissRequest: () -> Unit,
    onCarAdded: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    AuthRepository.getUserData { userData ->
        if (userData != null) {
            val car = Cars(
                id = carToEdit?.id ?: (0..999999).random(),
                name = formState.name,
                plate = formState.plate,
                brand = formState.brand,
                model = formState.model,
                color = formState.color,
                ownerName = userData.name,
                ownerId = userData.id,
                parkingId = carToEdit?.parkingId ?: 0
            )
            scope.launch {
                if (isEdit && carToEdit != null && carToEdit.plate != formState.plate) {
                    ParkingRepository.deleteCar(carToEdit.plate) { _, _ ->
                        ParkingRepository.addCar(car) { success, error ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Auto editado correctamente")
                                    onDismissRequest()
                                    onCarAdded?.invoke()
                                } else {
                                    snackbarHostState.showSnackbar("Error: $error")
                                }
                            }
                        }
                    }
                } else {
                    ParkingRepository.addCar(car) { success, error ->
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar(if (isEdit) "Auto editado correctamente" else "Auto guardado correctamente")
                                onDismissRequest()
                                onCarAdded?.invoke()
                            } else {
                                snackbarHostState.showSnackbar("Error: $error")
                            }
                        }
                    }
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("No se pudo obtener el usuario actual")
            }
        }
    }
}

@Composable
private fun AddCarCard(
    formState: CarFormState,
    onFormStateChange: (CarFormState) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    isEdit: Boolean,
    customTitle: String?,
    snackbarHostState: SnackbarHostState,
    formErrors: CarFormErrors
) {
    Card(
        colors = CardDefaults.cardColors(colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = customTitle ?: if (isEdit) "Editar información del auto" else "Agregar nuevo Auto",
                color = colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            CarFormFields(
                formState = formState,
                onValueChange = onFormStateChange,
                colors = colors,
                formErrors = formErrors
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.height(56.dp).weight(1f).padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        if (isEdit) "Actualizar" else "Guardar",
                        fontWeight = FontWeight.Bold, 
                        color = colors.onPrimary, 
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.height(56.dp).weight(1f).padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold, color = colors.onSecondary, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun CarFormFields(
    formState: CarFormState,
    onValueChange: (CarFormState) -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    formErrors: CarFormErrors
) {
    OutlinedTextField(
        value = formState.name,
        onValueChange = { onValueChange(formState.copy(name = it)) },
        label = { Text("Nombre del Auto", color = colors.text) },
        singleLine = true,
        isError = formErrors.name != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
    if (formErrors.name != null) {
        Text(
            formErrors.name!!,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
    OutlinedTextField(
        value = formState.plate,
        onValueChange = { onValueChange(formState.copy(plate = it)) },
        label = { Text("Placa", color = colors.text) },
        singleLine = true,
        isError = formErrors.plate != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
    if (formErrors.plate != null) {
        Text(
            formErrors.plate!!,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
    OutlinedTextField(
        value = formState.brand,
        onValueChange = { onValueChange(formState.copy(brand = it)) },
        label = { Text("Marca", color = colors.text) },
        singleLine = true,
        isError = formErrors.brand != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
    if (formErrors.brand != null) {
        Text(
            formErrors.brand!!,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
    OutlinedTextField(
        value = formState.model,
        onValueChange = { onValueChange(formState.copy(model = it)) },
        label = { Text("Modelo", color = colors.text) },
        singleLine = true,
        isError = formErrors.model != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
    if (formErrors.model != null) {
        Text(
            formErrors.model!!,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
    OutlinedTextField(
        value = formState.color,
        onValueChange = { onValueChange(formState.copy(color = it)) },
        label = { Text("Color", color = colors.text) },
        singleLine = true,
        isError = formErrors.color != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
    if (formErrors.color != null) {
        Text(
            formErrors.color!!,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 2.dp),
            textAlign = TextAlign.Start
        )
    }
}

// Validación que retorna errores por campo
private fun validateCarFormFields(formState: CarFormState): CarFormErrors {
    return CarFormErrors(
        name = when {
            formState.name.isBlank() -> "El nombre del auto es obligatorio"
            formState.name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        },
        plate = when {
            formState.plate.isBlank() -> "La placa es obligatoria"
            formState.plate.length < 3 -> "La placa debe tener al menos 3 caracteres"
            else -> null
        },
        brand = when {
            formState.brand.isBlank() -> "La marca es obligatoria"
            formState.brand.length < 2 -> "La marca debe tener al menos 2 caracteres"
            else -> null
        },
        model = when {
            formState.model.isBlank() -> "El modelo es obligatorio"
            formState.model.length < 2 -> "El modelo debe tener al menos 2 caracteres"
            else -> null
        },
        color = when {
            formState.color.isBlank() -> "El color es obligatorio"
            formState.color.length < 2 -> "El color debe tener al menos 2 caracteres"
            else -> null
        }
    )
}
