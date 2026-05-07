package com.example.spotagame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.spotagame.ui.theme.SpotAGameTheme
import com.google.android.gms.maps.MapsInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {}
        enableEdgeToEdge()
        setContent {
            SpotAGameTheme {
                SpotAGameApp()
            }
        }
    }
}