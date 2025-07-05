package com.espoch.qrcontrol.data

import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns
import com.espoch.qrcontrol.model.user
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Repositorio de autenticación y gestión de usuarios con Firebase
 * 
 * Este objeto maneja todas las operaciones relacionadas con la autenticación:
 * - Registro de usuarios con email/contraseña
 * - Inicio de sesión con email/contraseña
 * - Autenticación con Google
 * - Gestión de sesiones
 * - Almacenamiento de datos de usuario en Firestore
 * 
 * Los usuarios se almacenan en Firestore con roles (user/supervisor)
 * y se sincronizan con Firebase Authentication
 */
object AuthRepository {
    // Instancias de Firebase para autenticación y base de datos
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore

    /**
     * Registra un nuevo usuario en el sistema
     * 
     * Crea una cuenta en Firebase Authentication y almacena los datos
     * del usuario en Firestore con rol 'user' por defecto
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param name Nombre completo del usuario
     * @param onResult Callback con resultado: (éxito, error, rol)
     */
    fun signup(
        email: String,
        password: String,
        name: String,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    // Datos del usuario a almacenar en Firestore
                    val userData = hashMapOf(
                        "id" to userId,
                        "name" to name,
                        "email" to email,
                        "role" to "user", // Rol por defecto
                        "createdAt" to System.currentTimeMillis().toString(),
                        "updatedAt" to System.currentTimeMillis().toString()
                    )
                    // Guarda los datos en Firestore
                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener { 
                            // Guarda la sesión local después del registro exitoso
                            val userModel = user(
                                id = userId,
                                name = name,
                                email = email,
                                password = "",
                                role = "user",
                                createdAt = System.currentTimeMillis().toString(),
                                updatedAt = System.currentTimeMillis().toString()
                            )
                            SessionManager.saveSession(userModel, "user")
                            onResult(true, null, "user") 
                        }
                        .addOnFailureListener { e -> onResult(false, e.localizedMessage, null) }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error desconocido", null)
                }
            }
    }

    /**
     * Inicia sesión con email y contraseña
     * 
     * Autentica al usuario con Firebase y obtiene su rol desde Firestore
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param onResult Callback con resultado: (éxito, error, rol)
     */
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    // Obtiene el rol del usuario desde Firestore
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val role = document.getString("role") ?: "user"
                                // Guarda la sesión local después del login exitoso
                                val userModel = user(
                                    id = userId,
                                    name = document.getString("name") ?: "",
                                    email = document.getString("email") ?: "",
                                    password = "",
                                    role = role,
                                    createdAt = document.getString("createdAt") ?: "",
                                    updatedAt = document.getString("updatedAt") ?: ""
                                )
                                SessionManager.saveSession(userModel, role)
                                onResult(true, null, role)
                            } else {
                                onResult(false, "Usuario no encontrado en la base de datos", null)
                            }
                        }
                        .addOnFailureListener { e -> onResult(false, e.localizedMessage, null) }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error desconocido", null)
                }
            }
    }

    /**
     * Cierra la sesión del usuario actual
     * 
     * Limpia la sesión de Firebase Authentication y la sesión local
     */
    fun logout() { 
        auth.signOut()
        SessionManager.clearSession()
    }

    /**
     * Obtiene el usuario autenticado actual
     * 
     * @return Usuario actual de Firebase Authentication (solo datos básicos)
     */
    fun getCurrentUser() = auth.currentUser

    /**
     * Autenticación con Google
     * 
     * Si el usuario no existe en Firestore, lo crea automáticamente
     * con rol 'user' por defecto
     * 
     * @param idToken Token de ID de Google
     * @param onResult Callback con resultado: (éxito, error, rol)
     */
    fun AuthWithGoogle(idToken: String, onResult: (Boolean, String?, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""
                    val email = user?.email ?: ""
                    val name = user?.displayName ?: ""
                    
                    // Verifica si el usuario ya existe en Firestore
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                // Usuario existe, obtiene su rol y guarda sesión
                                val role = document.getString("role") ?: "user"
                                val userModel = user(
                                    id = userId,
                                    name = document.getString("name") ?: name,
                                    email = document.getString("email") ?: email,
                                    password = "",
                                    role = role,
                                    createdAt = document.getString("createdAt") ?: "",
                                    updatedAt = document.getString("updatedAt") ?: ""
                                )
                                SessionManager.saveSession(userModel, role)
                                onResult(true, null, role)
                            } else {
                                // Usuario nuevo, lo crea en Firestore
                                val userData = hashMapOf(
                                    "id" to userId,
                                    "name" to name,
                                    "email" to email,
                                    "role" to "user", // Rol por defecto
                                    "createdAt" to System.currentTimeMillis().toString(),
                                    "updatedAt" to System.currentTimeMillis().toString()
                                )
                                db.collection("users").document(userId).set(userData)
                                    .addOnSuccessListener { 
                                        val userModel = user(
                                            id = userId,
                                            name = name,
                                            email = email,
                                            password = "",
                                            role = "user",
                                            createdAt = System.currentTimeMillis().toString(),
                                            updatedAt = System.currentTimeMillis().toString()
                                        )
                                        SessionManager.saveSession(userModel, "user")
                                        onResult(true, null, "user") 
                                    }
                                    .addOnFailureListener { e -> onResult(false, e.localizedMessage, null) }
                            }
                        }
                        .addOnFailureListener { e -> onResult(false, e.localizedMessage, null) }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error desconocido", null)
                }
            }
    }

    /**
     * Obtiene todos los datos del usuario actual desde Firestore
     * 
     * @param onResult Callback con los datos completos del usuario
     */
    fun getUserData(onResult: (user?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(null)
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Convierte los datos de Firestore al modelo user
                    val userData = user(
                        id = document.getString("id") ?: "",
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        password = "", // No exponer contraseña por seguridad
                        role = document.getString("role") ?: "user",
                        createdAt = document.getString("createdAt") ?: "",
                        updatedAt = document.getString("updatedAt") ?: ""
                    )
                    onResult(userData)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }
    
    /**
     * Verifica si hay una sesión local activa
     * 
     * @return true si hay una sesión guardada y válida, false en caso contrario
     */
    fun hasValidSession(): Boolean {
        return SessionManager.isLoggedIn() && !SessionManager.isSessionExpired()
    }
    
    /**
     * Obtiene los datos del usuario desde la sesión local
     * 
     * @return Datos del usuario guardados localmente o null si no hay sesión
     */
    fun getLocalUserData(): user? {
        return SessionManager.getUserData()
    }
    
    /**
     * Obtiene el rol del usuario desde la sesión local
     * 
     * @return Rol del usuario guardado localmente
     */
    fun getLocalUserRole(): String {
        return SessionManager.getUserRole()
    }
    
    /**
     * Refresca la sesión local
     * 
     * Se llama cuando el usuario interactúa con la app para mantener la sesión activa
     */
    fun refreshLocalSession() {
        SessionManager.refreshLastLogin()
    }
    
    /**
     * Obtiene un usuario específico por su ID
     * 
     * @param userId ID del usuario a buscar
     * @param onResult Callback con los datos del usuario o null si no existe
     */
    fun getUserById(userId: String, onResult: (user?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = user(
                        id = document.getString("id") ?: "",
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        password = "",
                        role = document.getString("role") ?: "user",
                        createdAt = document.getString("createdAt") ?: "",
                        updatedAt = document.getString("updatedAt") ?: ""
                    )
                    onResult(userData)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }
}

/**
 * Validaciones básicas para formularios de autenticación
 * 
 * Proporciona funciones de validación para email, contraseña y nombre de usuario
 */
object BasicValidations {
    /**
     * Valida que el email tenga formato correcto
     */
    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    
    /**
     * Valida que la contraseña tenga al menos 6 caracteres
     */
    fun isValidPassword(password: String): Boolean = password.length >= 6
    
    /**
     * Valida que el nombre de usuario tenga al menos 3 caracteres
     */
    fun isValidUsername(username: String): Boolean = username.isNotEmpty() && username.length >= 3
}