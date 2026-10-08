package com.example.ui.theme

import androidx.compose.runtime.Composable
import com.example.model.FridayThemeId

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    FridaySystemTheme(themeId = FridayThemeId.CYAN, content = content)
}
