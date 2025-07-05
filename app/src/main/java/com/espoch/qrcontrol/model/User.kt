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
    val id: String, // Identificador único del usuario (UID de Firebase Authentication)
    val name: String, // Nombre completo del usuario
    val email: String, // Correo electrónico del usuario (usado para login)
    val password: String, // Contraseña del usuario (UID generado por Firebase Auth)
    val role: String, // Rol del usuario: "supervisor" (administrador) o "user" (usuario normal)
    val createdAt: String, // Fecha de creación del usuario en formato ISO
    val updatedAt: String // Fecha de última actualización del usuario en formato ISO
)