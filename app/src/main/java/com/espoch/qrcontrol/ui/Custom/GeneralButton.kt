package com.espoch.qrcontrol.ui.Custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.espoch.qrcontrol.ui.theme.QrColors

@Composable
fun GeneralButton(
    modifier: Modifier,
    painter: Painter,
    title: String,
    colors: QrColors
) {
    Box(
        modifier = modifier
            .border(1.dp, colors.primary, CircleShape)
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painter,
            contentDescription = "Icon",
            modifier = Modifier
                .padding(start = 20.dp)
                .size(20.dp)
        )
        Text(
            text = title,
            color = colors.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}
