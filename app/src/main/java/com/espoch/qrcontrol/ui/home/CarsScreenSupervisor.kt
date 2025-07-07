package com.espoch.qrcontrol.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.data.ParkingRepository
import com.espoch.qrcontrol.ui.theme.QrColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CarsScreenSupervisor(
    car: Cars,
    colors: QrColors
) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    // Obtener la hora de entrada real desde el historial activo
    LaunchedEffect(car.plate, car.parkingId) {
        if (car.parkingId != 0) {
            ParkingRepository.getActiveHistorialForCar(car.plate, car.parkingId) { historial ->
                entryDate = historial?.entryDate ?: ""
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isExpanded) 20.dp else 0.dp)
        ) {
            // Título clickeable que expande/contrae la información
            CarSupervisorTitle(
                car = car, 
                colors = colors, 
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded }
            )
            
            // Información expandible
            if (isExpanded) {
                CarSupervisorInfoRow(car, colors, entryDate)
                RegisterExitButton(colors) {
                    scope.launch {
                        ParkingRepository.registerCarExit(car.plate, car.parkingId) { success, error ->
                            message = if (success) "Salida registrada correctamente" else "Error: $error"
                        }
                    }
                }
                if (message.isNotEmpty()) {
                    Text(message, color = if (message.startsWith("Error")) colors.error else colors.primary)
                }
            }
        }
    }
}

@Composable
private fun CarSupervisorTitle(
    car: Cars, 
    colors: QrColors, 
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${car.plate}",
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Contraer" else "Expandir",
            tint = colors.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun CarSupervisorInfoRow(car: Cars, colors: QrColors, entryDate: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Hora de entrada: ${if (entryDate.isNotEmpty()) extractTimeFromDate(entryDate) else "-"}",
                color = colors.text,
                fontSize = 14.sp
            )
            Text("Marca: ${car.brand}", color = colors.text, fontSize = 14.sp)
            Text("Color: ${car.color}", color = colors.text, fontSize = 14.sp)
            Text(
                "Modelo: ${car.model}",
                color = colors.text,
                fontSize = 14.sp
            )
            Text(
                "Propietario: ${car.ownerName}",
                color = colors.text,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RegisterExitButton(colors: QrColors, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colors.error),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Registrar Salida",
            color = colors.onPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Extrae solo la hora de una fecha completa
 * 
 * @param dateString Fecha en formato "yyyy-MM-dd HH:mm:ss"
 * @return Hora en formato "HH:mm" o la fecha original si hay error
 */
private fun extractTimeFromDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        // Si hay error en el parsing, intenta extraer solo la hora manualmente
        try {
            val parts = dateString.split(" ")
            if (parts.size >= 2) {
                parts[1].substring(0, 5) // Toma HH:mm de la parte de tiempo
            } else {
                dateString
            }
        } catch (e2: Exception) {
            dateString
        }
    }
}
