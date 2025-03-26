package com.example.voithos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme // or darkColorScheme
import androidx.compose.runtime.Composable


private val LightColorScheme = lightColorScheme( // or darkColorScheme

    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

)

@Composable
fun VoithosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {


    MaterialTheme(
        colorScheme = LightColorScheme, // or DarkColorScheme
        typography = Typography,
        content = content
    )
}
