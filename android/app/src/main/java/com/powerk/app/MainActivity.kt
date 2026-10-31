package com.powerk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Server.init(this)
        Lang.init(this)
        Lang.applyStored(this)
        setContent {
            val systemDark = isSystemInDarkTheme()
            val appearance = remember { getSharedPreferences("appearance", MODE_PRIVATE) }
            var dark by remember { mutableStateOf(if (appearance.contains("dark")) appearance.getBoolean("dark", systemDark) else systemDark) }
            LaunchedEffect(dark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            PowerkTheme(dark) {
                PowerkApp(dark, onThemeChange = {
                    dark = !dark
                    appearance.edit().putBoolean("dark", dark).apply()
                })
            }
        }
    }
}
