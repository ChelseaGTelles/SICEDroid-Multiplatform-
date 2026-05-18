package com.example.sicedroidmultiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Inicializamos el contexto para Room
        initAndroidContext(this)

        setContent {
            App()
        }
    }
}

@Composable
fun AppAndroidPreview() {
    App()
}
