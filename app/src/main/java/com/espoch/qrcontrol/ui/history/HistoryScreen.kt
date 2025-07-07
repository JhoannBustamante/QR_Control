package com.espoch.qrcontrol.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espoch.qrcontrol.data.ParkingRepository
import com.espoch.qrcontrol.data.AuthRepository
import com.espoch.qrcontrol.model.HistorialParking
import com.espoch.qrcontrol.model.user
import com.espoch.qrcontrol.ui.theme.qrColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de historial de estacionamiento para supervisores
 * 
 * Esta pantalla permite a los supervisores:
 * - Ver todo el historial de estacionamiento
 * - Buscar registros por placa, usuario o espacio
 * - Filtrar por fechas
 * - Ver detalles completos de cada registro
 * 
 * Solo es accesible para usuarios con rol "supervisor"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    userRole: String,
    isDarkMode: Boolean
) {
    val colors = qrColors(isDarkMode)
    
    // Estados para manejar la UI y datos
    var historialList by remember { mutableStateOf<List<HistorialParking>>(emptyList()) }
    var filteredList by remember { mutableStateOf<List<HistorialParking>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var userNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    // Carga el historial al iniciar la pantalla
    LaunchedEffect(Unit) {
        if (userRole == "supervisor") {
            ParkingRepository.getAllHistorialParking { historial ->
                historialList = historial.sortedByDescending { it.entryDate }
                filteredList = historialList
                isLoading = false
                
                // Carga los nombres de usuarios únicos
                val uniqueUserIds = historial.map { it.userId }.distinct()
                loadUserNames(uniqueUserIds) { names ->
                    userNames = names
                }
            }
        }
    }
    
    // Filtra la lista cuando cambia la búsqueda
    LaunchedEffect(searchQuery, historialList, userNames) {
        filteredList = if (searchQuery.isEmpty()) {
            historialList
        } else {
            historialList.filter { historial ->
                val userName = userNames[historial.userId] ?: historial.userId
                historial.carId.contains(searchQuery, ignoreCase = true) ||
                userName.contains(searchQuery, ignoreCase = true) ||
                historial.parkingSpotId.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Estacionamiento",
                        color = colors.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas rápidas
            StatisticsCards(historialList, colors)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de historial
            if (isLoading) {
                LoadingIndicator(colors)
            } else if (filteredList.isEmpty()) {
                EmptyState(searchQuery, colors)
            } else {
                HistorialList(
                    historialList = filteredList,
                    colors = colors,
                    userNames = userNames
                )
            }
        }
    }
}

/**
 * Carga los nombres de usuarios desde Firestore
 * 
 * @param userIds Lista de IDs de usuarios únicos
 * @param onComplete Callback con el mapa de userId -> nombre
 */
private fun loadUserNames(userIds: List<String>, onComplete: (Map<String, String>) -> Unit) {
    val namesMap = mutableMapOf<String, String>()
    var completedCount = 0
    
    if (userIds.isEmpty()) {
        onComplete(emptyMap())
        return
    }
    
    userIds.forEach { userId ->
        // Busca el usuario en Firestore
        AuthRepository.getUserById(userId) { user ->
            namesMap[userId] = user?.name ?: "Usuario Desconocido"
            completedCount++
            
            if (completedCount == userIds.size) {
                onComplete(namesMap)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Buscar por placa, usuario ...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = colors.text
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        tint = colors.text
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.primary.copy(alpha = 0.5f),
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.primary.copy(alpha = 0.7f)
        )
    )
}

@Composable
private fun StatisticsCards(
    historialList: List<HistorialParking>,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Total de registros
        StatisticCard(
            title = "Total",
            value = historialList.size.toString(),
            colors = colors,
            modifier = Modifier.weight(1f)
        )
        
        // Registros activos (sin fecha de salida)
        StatisticCard(
            title = "Activos",
            value = historialList.count { it.exitDate.isEmpty() }.toString(),
            colors = colors,
            modifier = Modifier.weight(1f)
        )
        
        // Registros completados
        StatisticCard(
            title = "Completados",
            value = historialList.count { it.exitDate.isNotEmpty() }.toString(),
            colors = colors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = colors.text.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LoadingIndicator(colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun EmptyState(
    searchQuery: String,
    colors: com.espoch.qrcontrol.ui.theme.QrColors
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (searchQuery.isEmpty()) {
                "No hay registros de estacionamiento"
            } else {
                "No se encontraron resultados para '$searchQuery'"
            },
            color = colors.text.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun HistorialList(
    historialList: List<HistorialParking>,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    userNames: Map<String, String>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historialList) { historial ->
            HistorialItem(
                historial = historial,
                colors = colors,
                userNames = userNames
            )
        }
    }
}

@Composable
private fun HistorialItem(
    historial: HistorialParking,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    userNames: Map<String, String>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con placa y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = historial.carId,
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Estado del registro
                val isActive = historial.exitDate.isEmpty()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) colors.error else colors.primary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isActive) "ACTIVO" else "COMPLETADO",
                        color = colors.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información del registro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Usuario: \n ${userNames[historial.userId] ?: historial.userId}",
                        color = colors.text.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Espacio: ${historial.parkingSpotId}",
                        color = colors.text.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Entrada: ${formatDate(historial.entryDate)}",
                        color = colors.text.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    if (historial.exitDate.isNotEmpty()) {
                        Text(
                            text = "Salida: ${formatDate(historial.exitDate)}",
                            color = colors.text.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Duración (si está completado)
            if (historial.exitDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Duración: ${calculateDuration(historial.entryDate, historial.exitDate)}",
                    color = colors.text.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Formatea una fecha para mostrar de manera legible
 */
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Calcula la duración entre entrada y salida
 */
private fun calculateDuration(entryDate: String, exitDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val entry = inputFormat.parse(entryDate)
        val exit = inputFormat.parse(exitDate)
        
        if (entry != null && exit != null) {
            val diffInMillis = exit.time - entry.time
            val hours = diffInMillis / (1000 * 60 * 60)
            val minutes = (diffInMillis % (1000 * 60 * 60)) / (1000 * 60)
            
            when {
                hours > 0 -> "${hours}h ${minutes}m"
                else -> "${minutes}m"
            }
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
} 