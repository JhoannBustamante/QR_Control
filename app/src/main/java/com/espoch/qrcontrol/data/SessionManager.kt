package com.espoch.qrcontrol.data

import android.content.Context
import android.content.SharedPreferences
import com.espoch.qrcontrol.model.user
import com.google.gson.Gson

/**
 * Gestor de sesión local para mantener la autenticación persistente
 * 
 * Este objeto maneja el almacenamiento local de datos de sesión usando SharedPreferences:
 * - Guarda información del usuario autenticado
 * - Mantiene el estado de autenticación
 * - Permite verificar si hay una sesión activa
 * - Limpia los datos al cerrar sesión
 * 
 * Los datos se mantienen incluso cuando la app se cierra completamente
 */
object SessionManager {
    private const val PREF_NAME = "QRControlSession"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_DATA = "userData"
    private const val KEY_USER_ROLE = "userRole"
    private const val KEY_LAST_LOGIN = "lastLogin"
    private const val KEY_DARK_MODE = "darkMode"
    
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    /**
     * Inicializa el SessionManager con el contexto de la aplicación
     * 
     * @param context Contexto de la aplicación
     */
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Guarda los datos de sesión del usuario
     * 
     * @param userData Datos completos del usuario
     * @param role Rol del usuario (user/supervisor)
     */
    fun saveSession(userData: user, role: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_DATA, gson.toJson(userData))
        editor.putString(KEY_USER_ROLE, role)
        editor.putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
        editor.apply()
    }
    
    /**
     * Verifica si hay una sesión activa
     * 
     * @return true si hay una sesión guardada, false en caso contrario
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Obtiene los datos del usuario guardados en la sesión
     * 
     * @return Datos del usuario o null si no hay sesión
     */
    fun getUserData(): user? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, user::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Obtiene el rol del usuario guardado en la sesión
     * 
     * @return Rol del usuario o "user" por defecto
     */
    fun getUserRole(): String {
        return sharedPreferences.getString(KEY_USER_ROLE, "user") ?: "user"
    }
    
    /**
     * Obtiene la fecha del último login
     * 
     * @return Timestamp del último login o 0 si no hay sesión
     */
    fun getLastLogin(): Long {
        return sharedPreferences.getLong(KEY_LAST_LOGIN, 0)
    }
    
    /**
     * Limpia todos los datos de sesión
     * 
     * Se llama cuando el usuario cierra sesión
     */
    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    
    /**
     * Actualiza los datos del usuario en la sesión
     * 
     * @param userData Nuevos datos del usuario
     */
    fun updateUserData(userData: user) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_DATA, gson.toJson(userData))
        editor.apply()
    }
    
    /**
     * Verifica si la sesión ha expirado
     * 
     * @param maxSessionDuration Duración máxima de la sesión en milisegundos (por defecto 30 días)
     * @return true si la sesión ha expirado, false en caso contrario
     */
    fun isSessionExpired(maxSessionDuration: Long = 30 * 24 * 60 * 60 * 1000L): Boolean {
        val lastLogin = getLastLogin()
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastLogin) > maxSessionDuration
    }
    
    /**
     * Refresca la fecha del último login
     * 
     * Se llama cuando el usuario interactúa con la app
     */
    fun refreshLastLogin() {
        val editor = sharedPreferences.edit()
        editor.putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
        editor.apply()
    }
    
    /**
     * Guarda la preferencia del modo oscuro/claro
     * 
     * @param isDarkMode true para modo oscuro, false para modo claro
     */
    fun saveDarkModePreference(isDarkMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_DARK_MODE, isDarkMode)
        editor.apply()
    }
    
    /**
     * Obtiene la preferencia guardada del modo oscuro/claro
     * 
     * @return true si está en modo oscuro, false si está en modo claro
     */
    fun getDarkModePreference(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, true) // true por defecto (modo oscuro)
    }
} 