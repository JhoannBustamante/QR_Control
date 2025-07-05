package com.espoch.qrcontrol.model

import org.json.JSONObject

/**
 * Modelo de datos para representar un vehículo en el sistema
 * 
 * Esta clase define la estructura de datos de un vehículo que incluye:
 * - Información de identificación (ID, placa)
 * - Información del vehículo (nombre, marca, modelo, color)
 * - Información del propietario (nombre e ID)
 * - Estado de estacionamiento (ID del espacio asignado)
 * 
 * Los vehículos se pueden asignar a espacios de estacionamiento específicos
 * y están vinculados a sus propietarios a través del ownerId
 */
data class Cars(
    val id: Int = 0, // Identificador único del vehículo en la base de datos
    val name: String = "", // Nombre o alias del vehículo (ej: "Mi auto")
    val plate: String = "", // Placa/licencia del vehículo (identificación oficial)
    val brand: String = "", // Marca del vehículo (ej: Toyota, Ford)
    val model: String = "", // Modelo específico del vehículo (ej: Corolla, Focus)
    val color: String = "", // Color del vehículo
    val ownerName: String = "", // Nombre del propietario del vehículo
    val ownerId: String = "", // ID del usuario propietario (referencia a User.id)
    val parkingId: Int = 0 // ID del espacio de estacionamiento asignado (0 = no asignado)
) {

}

/**
 * Convierte el objeto Cars a formato JSON
 * 
 * @return String en formato JSON con todos los datos del vehículo
 */
fun Cars.toJson(): String = org.json.JSONObject(
    mapOf(
        "id" to id,
        "name" to name,
        "plate" to plate,
        "brand" to brand,
        "model" to model,
        "color" to color,
        "ownerName" to ownerName,
        "ownerId" to ownerId,
        "parkingId" to parkingId
    )
).toString()
