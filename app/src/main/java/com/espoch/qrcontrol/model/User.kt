package com.espoch.qrcontrol.model

/**
 * Modelo de datos para representar un usuario en el sistema
 * 
 * Esta clase define la estructura de datos de un usuario que incluye:
 * - Información de identificación (ID, email, contraseña)
 * - Información personal (nombre)
 * - Rol en el sistema (supervisor o usuario normal)
 * - Metadatos de auditoría (fechas de creación y actualización)
 * 
 * Los datos se sincronizan con Firebase Authentication y Firestore
 */
data class user(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)