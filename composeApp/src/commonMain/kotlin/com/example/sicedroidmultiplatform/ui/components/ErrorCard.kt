package com.example.sicedroidmultiplatform.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConnectionErrorCard(message: String, onRetry: (() -> Unit)? = null) {
    val msgLower = message.lowercase()
    val isNoInternet = msgLower.contains("sin conexión") || 
                       msgLower.contains("unable") || 
                       msgLower.contains("access") || 
                       msgLower.contains("connect") || 
                       msgLower.contains("internet") ||
                       msgLower.contains("host") ||
                       msgLower.contains("timeout") ||
                       msgLower.contains("refused") ||
                       msgLower.contains("failed")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNoInternet) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isNoInternet) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "¡Primero conéctate a Internet!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFD32F2F),
                    textAlign = TextAlign.Center
                )
                // Ya no mostramos el mensaje técnico (subtexto) aquí para mantenerlo limpio
            } else {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNoInternet) Color(0xFFD32F2F) else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reintentar", color = Color.White)
                }
            }
        }
    }
}
