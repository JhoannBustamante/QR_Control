package com.espoch.qrcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.espoch.qrcontrol.navigation.NavGraph
import com.espoch.qrcontrol.ui.theme.QRControlTheme
import com.espoch.qrcontrol.data.SessionManager

/**
 * Actividad principal de la aplicación QRControl
 * 
 * Esta es la entrada principal de la aplicación que:
 * - Configura el tema (modo oscuro/claro)
 * - Inicializa la navegación principal
 * - Habilita edge-to-edge para una experiencia visual completa
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita edge-to-edge para que la app use toda la pantalla
        enableEdgeToEdge()
        
        // Inicializa el SessionManager para persistencia de sesión
        SessionManager.init(this)
        
        setContent {
            // Estado para controlar el modo oscuro/claro de la aplicación
            var isDarkMode by remember { mutableStateOf(SessionManager.getDarkModePreference()) }
            
            // Aplica el tema y configura la navegación principal
            QRControlTheme(darkTheme = isDarkMode) {
                NavGraph(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { 
                        isDarkMode = !isDarkMode
                        // Guarda la preferencia del tema
                        SessionManager.saveDarkModePreference(isDarkMode)
                    }
                )
            }
        }
    }
}
