package com.example.rshotsbilling.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import com.example.rshotsbilling.utils.UserSession

// GLOBAL BACKGROUND GRADIENT (Dynamic based on company colors)
@Composable
fun getAppGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            UserSession.primaryColor.copy(alpha = 0.85f),
            UserSession.secondaryColor.copy(alpha = 0.85f)
        )
    )
}

@Composable
fun RshotsBillingTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = UserSession.primaryColor,
        onPrimary = Color.Black,
        secondary = UserSession.secondaryColor,
        onSecondary = Color.White,
        tertiary = GradientRed,
        onTertiary = Color.White,
        surface = Color.White.copy(alpha = 0.8f),
        background = Color.Transparent
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
