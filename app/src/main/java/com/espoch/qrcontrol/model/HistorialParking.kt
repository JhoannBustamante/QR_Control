package com.espoch.qrcontrol.model

/**
 * Modelo de datos para representar el historial de estacionamiento
 * 
 * Esta clase define la estructura de datos para registrar las entradas
 * y salidas de vehículos en espacios de estacionamiento específicos.
 * 
 * Cada registro representa una sesión de estacionamiento completa:
 * - Entrada: cuando un vehículo ocupa un espacio
 * - Salida: cuando el vehículo libera el espacio
 * 
 * Los datos se almacenan en Firestore para auditoría y análisis
 */
data class HistorialParking(
    val id: Int, // Identificador único del registro de entrada/salida
    val userId: String, // ID del usuario que realizó la operación
    val carId: String, // Placa del vehículo que se estacionó
    val parkingSpotId: String, // ID del espacio de estacionamiento utilizado
    val entryDate: String, // Fecha y hora de entrada al estacionamiento (formato ISO)
    val exitDate: String // Fecha y hora de salida del estacionamiento (vacío si aún está estacionado)
){

}