package com.espoch.qrcontrol.ui.Qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.espoch.qrcontrol.model.Cars
import com.espoch.qrcontrol.model.toJson
import com.espoch.qrcontrol.ui.components.generateQrBitmap
import com.espoch.qrcontrol.ui.theme.qrColors

@Composable
fun QRCodeViewScreen(
    car: Cars,
    isDarkMode: Boolean,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit,
    qrSize: Dp = 300.dp
) {
    val colors = qrColors(isDarkMode)
    val qrData = remember(car) { car.toJson() }
    val qrBitmap = remember(qrData, qrSize) { generateQrBitmap(qrData, qrSize.value.toInt()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(colors.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                QRTitle(car, colors)
                QRSubtitle(car, colors)
                QRImageCard(qrBitmap.asImageBitmap(), colors, qrSize, car)
            }
        }
    }
}

@Composable
private fun QRTitle(car: Cars, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Text(
        text = car.plate,
        color = colors.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun QRSubtitle(car: Cars, colors: com.espoch.qrcontrol.ui.theme.QrColors) {
    Text(
        text = "${car.brand} ${car.model} - ${car.name}",
        color = colors.primary.copy(alpha = 0.8f),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 2.dp)
    )
    Text(
        text = "Propietario: ${car.ownerName}",
        color = colors.primary.copy(alpha = 0.7f),
        fontSize = 14.sp
    )
}

@Composable
private fun QRImageCard(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    colors: com.espoch.qrcontrol.ui.theme.QrColors,
    size: Dp,
    car: Cars
) {
    Card(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Código QR del auto ${car.plate} de ${car.ownerName}" },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Código QR generado para el auto ${car.plate} de ${car.ownerName}",
                modifier = Modifier.fillMaxSize().padding(24.dp)
            )
        }
    }
}
