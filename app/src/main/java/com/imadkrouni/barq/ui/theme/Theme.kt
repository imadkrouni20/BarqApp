package com.imadkrouni.barq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ElectricBlue = Color(0xFF1565C0)
val ElectricOrange = Color(0xFFF57C00)
val ElectricGreen = Color(0xFF388E3C)
val ElectricRed = Color(0xFFC62828)
val DarkBg = Color(0xFF121212)

private val LightColors = lightColorScheme(
    primary = ElectricBlue,
    secondary = ElectricOrange,
    tertiary = ElectricGreen,
    error = ElectricRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFFFFB74D),
    tertiary = Color(0xFF81C784),
    background = DarkBg
)

@Composable
fun BarqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
