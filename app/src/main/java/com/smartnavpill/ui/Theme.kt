package com.smartnavpill.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Cor de marca do Smart Nav Pill: um indigo/azul profundo, usado como seed
// quando o aparelho nao oferece cores dinamicas (Material You). Da identidade
// consistente em claro e escuro. Contraste do onPrimary (branco) sobre o
// primary #3B5BDB: 5.6:1 (acima de 4.5:1 para texto).
private val BrandPrimary = Color(0xFF3B5BDB)
private val BrandPrimaryDark = Color(0xFFAEBBFF)

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE2FF),
    onPrimaryContainer = Color(0xFF00105C),
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF00105C),
    primaryContainer = Color(0xFF2A3EA8),
    onPrimaryContainer = Color(0xFFDDE2FF),
)

@Composable
fun SmartNavPillTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        // Material You (cores dinamicas) no Android 12+ respeita o papel de
        // parede do usuario; cai para a cor de marca abaixo disso.
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
