package com.espoch.qrcontrol.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.ui.Qr.AddNewCarScreen
import com.espoch.qrcontrol.ui.Qr.QRCodeViewScreen
import com.espoch.qrcontrol.ui.theme.qrColors
import kotlinx.coroutines.launch
import com.espoch.qrcontrol.data.ParkingRepository

private enum class DialogType { NONE, QR, EDIT, DELETE }

/**
 * Tarjeta de información y acciones para un auto registrado por el usuario.
 */
@Composable
fun CarsScreenUser(
    car: Cars,
    isDarkMode: Boolean,
    onCarUpdated: (() -> Unit)? = null
) {
    val colors = qrColors(isDarkMode)
    var dialogType by remember { mutableStateOf(DialogType.NONE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CarHeader(car, colors)
            CarActionButtons(
                colors = colors,
                onShowQR = { dialogType = DialogType.QR },
                onEdit = { dialogType = DialogType.EDIT },
                onDelete = { dialogType = DialogType.DELETE }
            )
        }
        when (dialogType) {
            DialogType.QR -> QRCodeViewScreen(
                isDarkMode = isDarkMode,
                onDismissRequest = { dialogType = DialogType.NONE },
                car = car,
                onCancel = { dialogType = DialogType.NONE }
            )
            DialogType.EDIT -> AddNewCarScreen(
                isDarkMode = isDarkMode,
                onDismissRequest = { dialogType = DialogType.NONE },
                onCarAdded = {
                    dialogType = DialogType.NONE
                    onCarUpdated?.invoke()
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
                                onCarUpdated?.invoke()
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
}

@Composable
private fun CarHeader(car: Cars, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Text(
        text = car.plate,
        color = colors.primary,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp),
        textAlign = TextAlign.Center
    )
    Text(
        text = "${car.name} - ${car.brand} ${car.model} - ${car.color}",
        color = colors.primary.copy(alpha = 0.8f),
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CarActionButtons(
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    onShowQR: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        ActionButton(text = "QR", colors = colors, onClick = onShowQR)
        ActionButton(text = "Editar", colors = colors, onClick = onEdit)
        ActionButton(text = "Eliminar", colors = colors, onClick = onDelete)
    }
}

@Composable
private fun ActionButton(text: String, colors: com.espoch.qrcontrol.ui.theme.QrColors, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
        modifier = Modifier.height(48.dp).padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            color = colors.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    car: Cars,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
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
