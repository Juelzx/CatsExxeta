package com.example.catsexxeta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.catsexxeta.core.designsystem.CatsExxetaTheme
import com.example.catsexxeta.navigation.CatNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatsExxetaTheme {
                CatNavHost()
            }
        }
    }
}