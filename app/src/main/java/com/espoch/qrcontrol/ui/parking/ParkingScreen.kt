package com.espoch.qrcontrol.ui.parking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.ui.theme.qrColors
import androidx.compose.ui.tooling.preview.Preview
import com.espoch.qrcontrol.model.ParkingSpot
import com.espoch.qrcontrol.data.ParkingRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.model.HistorialParking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.text.style.TextOverflow
@Composable
fun ParkingScreen(
    isDarkMode: Boolean,
    carToAssign: Cars? = null,
    userRole: String = "user",
    onParkingAssigned: (() -> Unit)? = null
) {
    val colors = qrColors(isDarkMode)
    var spots by remember { mutableStateOf<List<ParkingSpot>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        ParkingRepository.getParkingSpots { fetchedSpots ->
            spots = fetchedSpots.sortedBy { it.id }
        }
    }

    val available = spots.count { it.estado == "disponible" }
    val occupied = spots.count { it.estado == "ocupado" }
    val reserved = spots.count { it.estado == "reservado" }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        ParkingContent(
            spots = spots,
            colors = colors,
            carToAssign = carToAssign,
            userRole = userRole,
            onSpotSelected = { spotId ->
                if (carToAssign != null) {
                    handleAssignSpot(
                        spotId = spotId,
                        car = carToAssign,
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        onParkingAssigned = onParkingAssigned
                    )
                }
            },
            available = available,
            occupied = occupied,
            reserved = reserved,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun ParkingContent(
    spots: List<ParkingSpot>,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    carToAssign: Cars?,
    userRole: String,
    onSpotSelected: (Int) -> Unit,
    available: Int,
    occupied: Int,
    reserved: Int,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(innerPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con título e icono
        ParkingHeader(colors)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Resumen de estado mejorado
        EnhancedParkingStatusSummary(available, occupied, reserved, colors)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mapa de plazas con título mejorado
        ParkingMapSection(spots, colors, carToAssign, userRole, onSpotSelected)
    }
}

private fun handleAssignSpot(
    spotId: Int,
    car: Cars,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onParkingAssigned: (() -> Unit)?
) {
    scope.launch {
        ParkingRepository.updateParkingSpot(spotId, "ocupado", car.plate) { success, error ->
            scope.launch {
                if (success) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val now = sdf.format(Date())
                    val historial = HistorialParking(
                        id = (0..999999).random(),
                        userId = car.ownerId,
                        carId = car.plate,
                        parkingSpotId = spotId.toString(),
                        entryDate = now,
                        exitDate = ""
                    )
                    ParkingRepository.createHistorialParking(historial) { hSuccess, hError ->
                        scope.launch {
                            if (hSuccess) {
                                ParkingRepository.updateCarParkingId(car.plate, spotId) { carSuccess, carError ->
                                    scope.launch {
                                        if (carSuccess) {
                                            snackbarHostState.showSnackbar("Espacio asignado y registro creado")
                                            onParkingAssigned?.invoke()
                                        } else {
                                            snackbarHostState.showSnackbar("Error al actualizar auto: $carError")
                                        }
                                    }
                                }
                            } else {
                                snackbarHostState.showSnackbar("Error al crear historial: $hError")
                            }
                        }
                    }
                } else {
                    snackbarHostState.showSnackbar("Error al asignar espacio: $error")
                }
            }
        }
    }
}

@Composable
private fun ParkingHeader(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = "Estacionamiento",
            tint = colors.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Estacionamiento",
            color = colors.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EnhancedParkingStatusSummary(available: Int, occupied: Int, reserved: Int, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Estado del Estacionamiento",
                color = colors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStatusCard(
                    title = "Disponibles",
                    count = available,
                    icon = Icons.Default.CheckCircle,
                    color = colors.accentContainer,
                    textColor = colors.primary,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                EnhancedStatusCard(
                    title = "Ocupados",
                    count = occupied,
                    icon = Icons.Default.Clear,
                    color = colors.error,
                    textColor = colors.onPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                EnhancedStatusCard(
                    title = "Reservados",
                    count = reserved,
                    icon = Icons.Default.Lock,
                    color = colors.secondaryContainer,
                    textColor = colors.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatusCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.defaultMinSize(minWidth = 80.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ParkingMapSection(
    spots: List<ParkingSpot>,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    carToAssign: Cars?,
    userRole: String,
    onSpotSelected: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Título de la sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Mapa",
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mapa de Plazas",
                    color = colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

                        Spacer(modifier = Modifier.height(12.dp))
            
            // Grid de plazas compacto
            EnhancedParkingSpotGrid(spots, colors, carToAssign, userRole, onSpotSelected)
        }
    }
}

@Composable
private fun EnhancedParkingSpotGrid(
    spots: List<ParkingSpot>,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    carToAssign: Cars?,
    userRole: String,
    onSpotSelected: (Int) -> Unit
) {
    // Organizar espacios en filas de 8 para mejor visualización
    val columns = 8
    val rows = (spots.size + columns - 1) / columns // Redondear hacia arriba
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(columns) { colIndex ->
                    val spotIndex = rowIndex * columns + colIndex
                    if (spotIndex < spots.size) {
                        CompactParkingSpot(
                            spot = spots[spotIndex],
                            colors = colors,
                            carToAssign = carToAssign,
                            userRole = userRole,
                            onSpotSelected = onSpotSelected,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Espacio vacío para mantener el layout
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactParkingSpot(
    spot: ParkingSpot,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    carToAssign: Cars?,
    userRole: String,
    onSpotSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (spot.estado) {
        "disponible" -> colors.accentContainer
        "ocupado"    -> colors.error
        "reservado"  -> colors.secondaryContainer
        else          -> colors.primary
    }
    
    val textColor = when (spot.estado) {
        "disponible" -> colors.primary
        "ocupado"    -> colors.onPrimary
        "reservado"  -> colors.primary
        else          -> colors.onPrimary
    }
    
    val isAvailable = spot.estado == "disponible" && carToAssign != null
    
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .size(40.dp)
            .shadow(
                elevation = if (isAvailable) 6.dp else 2.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .let { if (isAvailable) it.clickable { onSpotSelected(spot.id) } else it },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isAvailable) 6.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = spot.id.toString(),
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (spot.estado == "ocupado") {
                    Text(
                        text = if (userRole == "supervisor") spot.plate else "O",
                        color = textColor,
                        fontSize = 6.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}



@Preview
@Composable
fun ParkingScreenPreview() {
    ParkingScreen(isDarkMode = false)
}
