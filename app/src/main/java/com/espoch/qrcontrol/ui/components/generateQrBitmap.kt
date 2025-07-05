package com.espoch.qrcontrol.ui.components

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Genera un código QR como Bitmap a partir de un texto
 * 
 * Esta función utiliza la librería ZXing para crear códigos QR
 * que pueden contener información como:
 * - Identificadores de vehículos
 * - Datos de estacionamiento
 * - URLs o cualquier texto
 * 
 * @param content Contenido a codificar en el QR (texto, URL, etc.)
 * @param size Tamaño del código QR en píxeles (por defecto 512x512)
 * @return Bitmap del código QR generado
 */
fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
    // Crea el escritor de códigos QR
    val writer = QRCodeWriter()
    
    // Genera la matriz de bits del código QR
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    
    // Crea un bitmap del tamaño especificado
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    
    // Convierte la matriz de bits a píxeles del bitmap
    for (x in 0 until size) {
        for (y in 0 until size) {
            // Píxeles negros para los módulos del QR, blancos para el fondo
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    
    return bitmap
}
