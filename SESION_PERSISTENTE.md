# Persistencia de Sesión en QRControl

## Descripción General

Se ha implementado un sistema de persistencia de sesión que mantiene al usuario autenticado incluso cuando la aplicación se cierra completamente. Esto mejora significativamente la experiencia del usuario al evitar que tenga que iniciar sesión cada vez que abre la app.

## Componentes Implementados

### 1. SessionManager
**Archivo:** `app/src/main/java/com/espoch/qrcontrol/data/SessionManager.kt`

**Funcionalidades:**
- Almacenamiento local de datos de sesión usando SharedPreferences
- Verificación de sesión activa
- Gestión de expiración de sesión (30 días por defecto)
- Refresco automático de la sesión
- Limpieza de datos al cerrar sesión
- **Persistencia de preferencias del tema (modo oscuro/claro)**

**Métodos principales:**
- `init(context)`: Inicializa el SessionManager
- `saveSession(userData, role)`: Guarda los datos de sesión
- `isLoggedIn()`: Verifica si hay sesión activa
- `getUserData()`: Obtiene datos del usuario
- `clearSession()`: Limpia la sesión
- `isSessionExpired()`: Verifica expiración
- `refreshLastLogin()`: Refresca la sesión
- `saveDarkModePreference(isDarkMode)`: Guarda preferencia del tema
- `getDarkModePreference()`: Obtiene preferencia del tema

### 2. AuthRepository Mejorado
**Archivo:** `app/src/main/java/com/espoch/qrcontrol/data/AuthRepository.kt`

**Nuevas funcionalidades:**
- Integración automática con SessionManager
- Guardado de sesión después de login/registro exitoso
- Limpieza de sesión al cerrar sesión
- Verificación de sesión local

**Métodos agregados:**
- `hasValidSession()`: Verifica sesión válida
- `getLocalUserData()`: Obtiene datos locales
- `getLocalUserRole()`: Obtiene rol local
- `refreshLocalSession()`: Refresca sesión

### 3. Navegación Inteligente
**Archivo:** `app/src/main/java/com/espoch/qrcontrol/navigation/NavGraph.kt`

**Funcionalidades:**
- Verificación automática de sesión al iniciar la app
- Navegación automática a Home si hay sesión válida
- Destino inicial dinámico basado en el estado de sesión

### 4. Inicialización en MainActivity
**Archivo:** `app/src/main/java/com/espoch/qrcontrol/MainActivity.kt`

**Funcionalidades:**
- Inicialización del SessionManager al crear la actividad
- Configuración temprana para persistencia
- **Carga de preferencia del tema guardada**
- **Guardado automático de cambios de tema**

## Flujo de Funcionamiento

### 1. Primer Inicio de Sesión
1. Usuario inicia sesión con email/contraseña o Google
2. AuthRepository autentica con Firebase
3. Se obtienen datos del usuario desde Firestore
4. SessionManager guarda los datos localmente
5. Usuario navega a Home

### 2. Reapertura de la App
1. MainActivity inicializa SessionManager
2. NavGraph verifica si hay sesión válida
3. Si hay sesión válida, navega directamente a Home
4. Si no hay sesión, muestra StartScreen

### 3. Interacción del Usuario
1. Al acceder a HomeScreen, se refresca la sesión
2. Al acceder a SettingsScreen, se refresca la sesión
3. La sesión se mantiene activa con cada interacción
4. **Los cambios de tema se guardan automáticamente**
5. **El tema se restaura al reabrir la app**

### 4. Cierre de Sesión
1. Usuario presiona "Cerrar Sesión" en Settings
2. AuthRepository cierra sesión de Firebase
3. SessionManager limpia datos locales
4. Usuario regresa a StartScreen

## Configuración de Backup

### Archivo de Reglas de Backup
**Archivo:** `app/src/main/res/xml/backup_rules.xml`

**Configuración:**
- Incluye las preferencias de sesión en el backup automático
- Los datos de sesión se respaldan automáticamente
- Se restauran al reinstalar la app (si el usuario lo permite)

## Características de Seguridad

### 1. Expiración de Sesión
- Sesión expira después de 30 días de inactividad
- Se puede configurar la duración máxima
- Verificación automática de expiración

### 2. Datos Sensibles
- No se almacenan contraseñas en SharedPreferences
- Solo se guardan datos básicos del usuario
- Información de autenticación manejada por Firebase

### 3. Limpieza Automática
- Datos se limpian al cerrar sesión
- Verificación de integridad de datos
- Manejo de errores en datos corruptos

## Beneficios Implementados

### 1. Experiencia de Usuario
- ✅ No requiere login repetitivo
- ✅ Acceso inmediato a la app
- ✅ Transición fluida entre sesiones
- ✅ **Tema persistente entre sesiones**
- ✅ **Preferencias de usuario mantenidas**

### 2. Persistencia Robusta
- ✅ Funciona incluso con app cerrada completamente
- ✅ Backup automático de datos de sesión
- ✅ Restauración automática al reinstalar

### 3. Seguridad
- ✅ Expiración automática de sesión
- ✅ Limpieza de datos al cerrar sesión
- ✅ Verificación de integridad

### 4. Rendimiento
- ✅ Verificación rápida de sesión local
- ✅ No requiere consultas a Firebase al iniciar
- ✅ Refresco automático con interacciones

## Uso en el Código

### Verificar Sesión
```kotlin
if (AuthRepository.hasValidSession()) {
    // Usuario tiene sesión válida
    navigateToHome()
} else {
    // Usuario necesita iniciar sesión
    navigateToLogin()
}
```

### Obtener Datos del Usuario
```kotlin
val userData = AuthRepository.getLocalUserData()
val userRole = AuthRepository.getLocalUserRole()
```

### Refrescar Sesión
```kotlin
AuthRepository.refreshLocalSession()
```

### Cerrar Sesión
```kotlin
AuthRepository.logout()
```

## Consideraciones Técnicas

### 1. Compatibilidad
- Funciona en Android API 29+ (minSdk actual)
- Compatible con todas las versiones de Android soportadas
- No requiere dependencias adicionales

### 2. Rendimiento
- Verificación de sesión en milisegundos
- Almacenamiento local eficiente
- Sin impacto en el rendimiento de la app

### 3. Mantenimiento
- Código modular y bien documentado
- Fácil de extender y modificar
- Separación clara de responsabilidades

## Próximas Mejoras Posibles

1. **Sincronización de Datos**: Actualizar datos locales cuando cambien en Firebase
2. **Múltiples Sesiones**: Soporte para múltiples cuentas de usuario
3. **Biometría**: Integración con autenticación biométrica
4. **Notificaciones**: Alertas cuando la sesión esté por expirar
5. **Analytics**: Seguimiento de patrones de uso de sesión 