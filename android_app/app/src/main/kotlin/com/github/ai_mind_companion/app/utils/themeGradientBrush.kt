package com.github.ai_mind_companion.app.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

@Composable
fun themeGradientBrush(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    return Brush.verticalGradient(
        colors = listOf(primary, secondary)
    )
}