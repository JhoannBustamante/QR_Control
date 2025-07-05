package com.espoch.qrcontrol.model

/**
 * Modelo de datos para representar un espacio de estacionamiento
 * 
 * Esta clase define la estructura de datos de un espacio de estacionamiento que incluye:
 * - Identificación única del espacio
 * - Estado actual (disponible, ocupado, reservado)
 * - Información del vehículo que ocupa el espacio (si está ocupado)
 * 
 * Los espacios de estacionamiento pueden estar en diferentes estados
 * y pueden ser asignados a vehículos específicos
 */
data class ParkingSpot(
    val id: Int, // Identificador único del espacio de estacionamiento
    val estado : String, // Estado del espacio: "disponible", "ocupado", "reservado"
    val plate : String // Placa del vehículo que ocupa el espacio (vacío si está disponible)
){
}