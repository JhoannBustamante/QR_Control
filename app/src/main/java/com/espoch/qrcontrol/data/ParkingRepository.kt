package com.espoch.qrcontrol.data

import androidx.compose.runtime.Composable
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.model.ParkingSpot
import com.espoch.qrcontrol.model.HistorialParking
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repositorio para la gestión de vehículos y estacionamiento
 * 
 * Este objeto maneja todas las operaciones relacionadas con:
 * - Gestión de vehículos (agregar, obtener, eliminar)
 * - Gestión de espacios de estacionamiento
 * - Historial de estacionamiento
 * - Asignación de vehículos a espacios
 * 
 * Los datos se almacenan en Firestore en las colecciones:
 * - "cars": información de vehículos
 * - "parking_spots": espacios de estacionamiento
 * - "historial_parking": registro de entradas y salidas
 */
object ParkingRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Agrega un nuevo vehículo al sistema
     * 
     * Usa la placa como identificador único del documento
     * 
     * @param car Datos del vehículo a agregar
     * @param onResult Callback con resultado: (éxito, error)
     */
    fun addCar(car: Cars, onResult: (Boolean, String?) -> Unit) {
        // Usamos la placa como id único, puedes cambiarlo si lo prefieres
        db.collection("cars").document(car.plate)
            .set(car)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Obtiene todos los vehículos de un propietario específico
     * 
     * @param ownerId ID del usuario propietario
     * @param onResult Callback con la lista de vehículos
     */
    fun getCarsByOwnerId(ownerId: String, onResult: (List<Cars>) -> Unit) {
        db.collection("cars")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { result ->
                val cars = result.documents.mapNotNull { doc ->
                    try {
                        Cars(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            name = doc.getString("name") ?: "",
                            plate = doc.getString("plate") ?: "",
                            brand = doc.getString("brand") ?: "",
                            model = doc.getString("model") ?: "",
                            color = doc.getString("color") ?: "",
                            ownerName = doc.getString("ownerName") ?: "",
                            ownerId = doc.getString("ownerId") ?: "",
                            parkingId = (doc.getLong("parkingId") ?: 0L).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(cars)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * Elimina un vehículo del sistema
     * 
     * @param plate Placa del vehículo a eliminar
     * @param onResult Callback con resultado: (éxito, error)
     */
    fun deleteCar(plate: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("cars").document(plate)
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Obtiene todos los espacios de estacionamiento
     * 
     * @param onResult Callback con la lista de espacios
     */
    fun getParkingSpots(onResult: (List<ParkingSpot>) -> Unit) {
        db.collection("parking_spots")
            .get()
            .addOnSuccessListener { result ->
                val spots = result.documents.mapNotNull { doc ->
                    try {
                        ParkingSpot(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            estado = doc.getString("estado") ?: "disponible",
                            plate = doc.getString("plate") ?: "null"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(spots)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * Actualiza el estado de un espacio de estacionamiento
     * 
     * @param spotId ID del espacio a actualizar
     * @param estado Nuevo estado ("disponible", "ocupado", "reservado")
     * @param plate Placa del vehículo que ocupa el espacio
     * @param onResult Callback con resultado: (éxito, error)
     */
    fun updateParkingSpot(spotId: Int, estado: String, plate: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("parking_spots").document(spotId.toString())
            .update(mapOf(
                "estado" to estado,
                "plate" to plate
            ))
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Crea un nuevo registro en el historial de estacionamiento
     * 
     * @param historial Datos del historial a crear
     * @param onResult Callback con resultado: (éxito, error)
     */
    fun createHistorialParking(historial: HistorialParking, onResult: (Boolean, String?) -> Unit) {
        val docRef = db.collection("historial_parking").document()
        docRef.set(historial)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Obtiene todos los vehículos del sistema
     * 
     * @param onResult Callback con la lista de todos los vehículos
     */
    fun getAllCars(onResult: (List<Cars>) -> Unit) {
        db.collection("cars")
            .get()
            .addOnSuccessListener { result ->
                val cars = result.documents.mapNotNull { doc ->
                    try {
                        Cars(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            name = doc.getString("name") ?: "",
                            plate = doc.getString("plate") ?: "",
                            brand = doc.getString("brand") ?: "",
                            model = doc.getString("model") ?: "",
                            color = doc.getString("color") ?: "",
                            ownerName = doc.getString("ownerName") ?: "",
                            ownerId = doc.getString("ownerId") ?: "",
                            parkingId = (doc.getLong("parkingId") ?: 0L).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(cars)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * Registra la salida de un vehículo del estacionamiento
     * 
     * Este proceso actualiza múltiples documentos:
     * 1. Actualiza el historial con la fecha de salida
     * 2. Libera el espacio de estacionamiento
     * 3. Desasigna el vehículo del espacio
     * 
     * @param carPlate Placa del vehículo que sale
     * @param parkingSpotId ID del espacio de estacionamiento
     * @param onResult Callback con resultado: (éxito, error)
     */
    fun registerCarExit(carPlate: String, parkingSpotId: Int, onResult: (Boolean, String?) -> Unit) {
        // 1. Buscar el historial activo (sin exitDate) para este auto y espacio
        db.collection("historial_parking")
            .whereEqualTo("carId", carPlate)
            .whereEqualTo("parkingSpotId", parkingSpotId.toString())
            .whereEqualTo("exitDate", "")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onResult(false, "No se encontró historial activo")
                    return@addOnSuccessListener
                }
                val doc = result.documents.first()
                val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                // 2. Actualizar exitDate en historial
                db.collection("historial_parking").document(doc.id)
                    .update("exitDate", now)
                    .addOnSuccessListener {
                        // 3. Actualizar parking_spots a disponible
                        db.collection("parking_spots").document(parkingSpotId.toString())
                            .update(mapOf(
                                "estado" to "disponible",
                                "plate" to "null"
                            ))
                            .addOnSuccessListener {
                                // 4. Actualizar parkingId en cars a 0
                                db.collection("cars").document(carPlate)
                                    .update("parkingId", 0)
                                    .addOnSuccessListener { onResult(true, null) }
                                    .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
                            }
                            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
                    }
                    .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
            }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Actualiza el ID del espacio de estacionamiento asignado a un vehículo
     * 
     * @param plate Placa del vehículo
     * @param parkingId ID del espacio de estacionamiento asignado
     * @param onResult Callback con resultado: (éxito, error)
     */
    fun updateCarParkingId(plate: String, parkingId: Int, onResult: (Boolean, String?) -> Unit) {
        db.collection("cars").document(plate)
            .update("parkingId", parkingId)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    /**
     * Obtiene el historial activo de un vehículo en un espacio específico
     * 
     * Busca un registro de historial sin fecha de salida (exitDate vacío)
     * 
     * @param carPlate Placa del vehículo
     * @param parkingSpotId ID del espacio de estacionamiento
     * @param onResult Callback con el historial activo o null
     */
    fun getActiveHistorialForCar(carPlate: String, parkingSpotId: Int, onResult: (HistorialParking?) -> Unit) {
        db.collection("historial_parking")
            .whereEqualTo("carId", carPlate)
            .whereEqualTo("parkingSpotId", parkingSpotId.toString())
            .whereEqualTo("exitDate", "")
            .get()
            .addOnSuccessListener { result ->
                val historial = result.documents.firstOrNull()?.let { doc ->
                    try {
                        HistorialParking(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            userId = doc.getString("userId") ?: "",
                            carId = doc.getString("carId") ?: "",
                            parkingSpotId = doc.getString("parkingSpotId") ?: "",
                            entryDate = doc.getString("entryDate") ?: "",
                            exitDate = doc.getString("exitDate") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(historial)
            }
            .addOnFailureListener { onResult(null) }
    }
    
    /**
     * Obtiene todo el historial de estacionamiento
     * 
     * @param onResult Callback con la lista completa del historial
     */
    fun getAllHistorialParking(onResult: (List<HistorialParking>) -> Unit) {
        db.collection("historial_parking")
            .get()
            .addOnSuccessListener { result ->
                val historialList = result.documents.mapNotNull { doc ->
                    try {
                        HistorialParking(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            userId = doc.getString("userId") ?: "",
                            carId = doc.getString("carId") ?: "",
                            parkingSpotId = doc.getString("parkingSpotId") ?: "",
                            entryDate = doc.getString("entryDate") ?: "",
                            exitDate = doc.getString("exitDate") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(historialList)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
