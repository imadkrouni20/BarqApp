package com.imadkrouni.barq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.imadkrouni.barq.ui.navigation.BarqNav
import com.imadkrouni.barq.ui.theme.BarqTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BarqTheme {
                Surface(Modifier.fillMaxSize()) {
                    BarqNav()
                }
            }
        }
    }
}
