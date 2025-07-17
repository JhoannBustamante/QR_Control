package com.espoch.qrcontrol.ui.Qr

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.concurrent.Executors
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.accompanist.permissions.PermissionState
import com.espoch.qrcontrol.ui.theme.qrColors

/**
 * Pantalla de escáner de códigos QR mejorada
 * 
 * Esta pantalla permite escanear códigos QR usando la cámara del dispositivo.
 * Características mejoradas:
 * - Overlay visual con guías de escaneo
 * - Botones de acción (cerrar, flash)
 * - Feedback visual durante el escaneo
 * - Interfaz más profesional y atractiva
 * - Solicita permisos de cámara automáticamente
 * - Usa CameraX para la captura de video
 * - Utiliza ML Kit para detectar códigos QR en tiempo real
 * 
 * @param onQrScanned Callback con el contenido del QR escaneado
 * @param onDismiss Función para cerrar la pantalla
 * @param isDarkMode Indica si el modo oscuro está activo
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onQrScanned: (String) -> Unit,
    onDismiss: () -> Unit = {},
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val colors = qrColors(isDarkMode)
    
    // Estados para manejar permisos y QR escaneado
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showPermissionDialog by remember { mutableStateOf(false) }
    var scannedQr by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }

    // Solicita permisos de cámara al iniciar
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // Si los permisos están concedidos, muestra la cámara con overlay
            cameraPermissionState.status.isGranted -> {
                CameraPreviewWrapper(
                    onQrCodeScanned = { qrContent ->
                        // Solo procesa el primer QR para evitar duplicados
                        if (scannedQr == null) {
                            scannedQr = qrContent
                            isScanning = false
                            onQrScanned(qrContent)
                        }
                    }
                )
                
                // Overlay con elementos de UI
                ScannerOverlay(
                    isScanning = isScanning,
                    isDarkMode = isDarkMode,
                    onClose = onDismiss
                )
            }
            // Si no se pueden mostrar permisos, muestra diálogo
            !cameraPermissionState.status.shouldShowRationale -> {
                showPermissionDialog = true
                if (showPermissionDialog) {
                    PermissionDialog(onDismiss, cameraPermissionState) { showPermissionDialog = false }
                }
            }
        }
    }
}

/**
 * Overlay visual para el escáner QR
 * 
 * Incluye guías de escaneo, botón de cerrar y textos informativos arriba
 * 
 * @param isScanning Estado de escaneo activo
 * @param isDarkMode Indica si el modo oscuro está activo
 * @param onClose Función para cerrar el escáner
 */
@Composable
private fun ScannerOverlay(
    isScanning: Boolean,
    isDarkMode: Boolean,
    onClose: () -> Unit
) {
    val colors = qrColors(isDarkMode)
    Box(modifier = Modifier.fillMaxSize()) {
        // Textos informativos en la parte superior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isScanning) "Coloca el código QR dentro del marco" else "¡Código QR detectado!",
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        color = colors.background.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "El escaneo es automático",
                color = colors.text.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        color = colors.background.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        // Área central con solo esquinas y fondo translúcido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Fondo translúcido dentro del área de escaneo
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(colors.background.copy(alpha = 0.25f), shape = RoundedCornerShape(20.dp))
            ) {}
            // Esquinas gruesas y coloridas
            val cornerLength = 40.dp
            val cornerThickness = 6.dp
            val cornerColor = MaterialTheme.colorScheme.primary
            // Esquina superior izquierda
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center)
            ) {
                Box(
                    Modifier
                        .offset(x = 0.dp, y = 0.dp)
                        .size(width = cornerLength, height = cornerThickness)
                        .background(cornerColor, RoundedCornerShape(topStart = 12.dp))
                )
                Box(
                    Modifier
                        .offset(x = 0.dp, y = 0.dp)
                        .size(width = cornerThickness, height = cornerLength)
                        .background(cornerColor, RoundedCornerShape(topStart = 12.dp))
                )
                // Esquina superior derecha
                Box(
                    Modifier
                        .offset(x = 280.dp - cornerLength, y = 0.dp)
                        .size(width = cornerLength, height = cornerThickness)
                        .background(cornerColor, RoundedCornerShape(topEnd = 12.dp))
                )
                Box(
                    Modifier
                        .offset(x = 280.dp - cornerThickness, y = 0.dp)
                        .size(width = cornerThickness, height = cornerLength)
                        .background(cornerColor, RoundedCornerShape(topEnd = 12.dp))
                )
                // Esquina inferior izquierda
                Box(
                    Modifier
                        .offset(x = 0.dp, y = 280.dp - cornerThickness)
                        .size(width = cornerLength, height = cornerThickness)
                        .background(cornerColor, RoundedCornerShape(bottomStart = 12.dp))
                )
                Box(
                    Modifier
                        .offset(x = 0.dp, y = 280.dp - cornerLength)
                        .size(width = cornerThickness, height = cornerLength)
                        .background(cornerColor, RoundedCornerShape(bottomStart = 12.dp))
                )
                // Esquina inferior derecha
                Box(
                    Modifier
                        .offset(x = 280.dp - cornerLength, y = 280.dp - cornerThickness)
                        .size(width = cornerLength, height = cornerThickness)
                        .background(cornerColor, RoundedCornerShape(bottomEnd = 12.dp))
                )
                Box(
                    Modifier
                        .offset(x = 280.dp - cornerThickness, y = 280.dp - cornerLength)
                        .size(width = cornerThickness, height = cornerLength)
                        .background(cornerColor, RoundedCornerShape(bottomEnd = 12.dp))
                )
            }
        }
        // Botón de cancelar en la parte inferior derecha
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Contenedor para la vista previa de la cámara
 * 
 * @param onQrCodeScanned Callback cuando se detecta un QR
 */
@Composable
private fun CameraPreviewWrapper(onQrCodeScanned: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(onQrCodeScanned)
    }
}

/**
 * Diálogo mejorado para solicitar permisos de cámara
 * 
 * Se muestra cuando el usuario no ha concedido permisos de cámara
 * 
 * @param onDismiss Función para cerrar el diálogo
 * @param cameraPermissionState Estado de los permisos de cámara
 * @param onRequest Función para solicitar permisos nuevamente
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDialog(
    onDismiss: () -> Unit,
    cameraPermissionState: PermissionState,
    onRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Permiso de cámara requerido",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = { 
            Text(
                text = "Para escanear códigos QR y gestionar el estacionamiento, la aplicación necesita acceso a la cámara de tu dispositivo.",
                textAlign = TextAlign.Justify
            ) 
        },
        confirmButton = {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) { 
                Text("Permitir acceso") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancelar") 
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Vista previa de la cámara con detección de códigos QR
 * 
 * Configura CameraX y ML Kit para capturar video y detectar QR en tiempo real
 * 
 * @param onQrCodeScanned Callback cuando se detecta un QR
 */
@Composable
private fun CameraPreview(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var alreadyScanned by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()
        
        // Configura la vista previa de la cámara
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Configura ML Kit para detectar códigos de barras/QR
        val scanner = BarcodeScanning.getClient()
        val imageAnalyzer = ImageAnalysis.Builder().build().also { analysisUseCase ->
            analysisUseCase.setAnalyzer(executor) { imageProxy: ImageProxy ->
                if (!alreadyScanned) {
                    // Procesa cada frame para detectar QR
                    processImageProxy(imageProxy, scanner) { qrContent ->
                        if (!alreadyScanned && qrContent != null) {
                            alreadyScanned = true
                            onQrCodeScanned(qrContent)
                        }
                    }
                } else {
                    imageProxy.close()
                }
            }
        }

        // Vincula los casos de uso a la cámara
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )
        
        // Limpia recursos al destruir
        onDispose {
            cameraProvider.unbindAll()
            executor.shutdown()
        }
    }
    
    // Renderiza la vista previa de la cámara
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

/**
 * Procesa un frame de la cámara para detectar códigos QR
 * 
 * Utiliza ML Kit para analizar la imagen y detectar códigos de barras/QR
 * 
 * @param imageProxy Frame de la cámara a procesar
 * @param scanner Instancia del scanner de ML Kit
 * @param onQrFound Callback con el contenido del QR detectado
 */
@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrFound: (String?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        // Convierte el frame a formato compatible con ML Kit
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        // Procesa la imagen para detectar códigos de barras/QR
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Obtiene el primer código QR detectado
                val qr = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                onQrFound(qr)
            }
            .addOnFailureListener {
                onQrFound(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}