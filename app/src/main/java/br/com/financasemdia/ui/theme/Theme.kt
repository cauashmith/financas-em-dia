package br.com.financasemdia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B6B3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA4F4B4),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = Color(0xFF4F6352),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E8D3),
    onSecondaryContainer = Color(0xFF0D1F12),
    tertiary = Color(0xFF3A6471),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBEEAF9),
    onTertiaryContainer = Color(0xFF001F27),
    background = Color(0xFFF6FBF3),
    onBackground = Color(0xFF181D18),
    surface = Color(0xFFF6FBF3),
    onSurface = Color(0xFF181D18),
    surfaceVariant = Color(0xFFDDE5DA),
    onSurfaceVariant = Color(0xFF414941),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5ED),
    surfaceContainer = Color(0xFFEAEFE7),
    surfaceContainerHigh = Color(0xFFE4EAE1),
    surfaceContainerHighest = Color(0xFFDFE4DC),
    outline = Color(0xFF717970),
    outlineVariant = Color(0xFFC1C9BE),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF89D89A),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF005228),
    onPrimaryContainer = Color(0xFFA4F4B4),
    secondary = Color(0xFFB6CCB8),
    onSecondary = Color(0xFF223526),
    secondaryContainer = Color(0xFF384B3B),
    onSecondaryContainer = Color(0xFFD2E8D3),
    tertiary = Color(0xFFA2CEDC),
    onTertiary = Color(0xFF023541),
    tertiaryContainer = Color(0xFF214C58),
    onTertiaryContainer = Color(0xFFBEEAF9),
    background = Color(0xFF101510),
    onBackground = Color(0xFFDFE4DC),
    surface = Color(0xFF101510),
    onSurface = Color(0xFFDFE4DC),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BE),
    surfaceContainerLowest = Color(0xFF0B0F0B),
    surfaceContainerLow = Color(0xFF181D18),
    surfaceContainer = Color(0xFF1C211C),
    surfaceContainerHigh = Color(0xFF262B26),
    surfaceContainerHighest = Color(0xFF313631),
    outline = Color(0xFF8B9389),
    outlineVariant = Color(0xFF414941),
)

@Immutable
data class FinanceColors(val income: Color, val expense: Color)

private val LightFinanceColors = FinanceColors(income = Color(0xFF1B6B3A), expense = Color(0xFFB3261E))
private val DarkFinanceColors = FinanceColors(income = Color(0xFF89D89A), expense = Color(0xFFFFB4AB))

private val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

object FinancasTheme {
    val financeColors: FinanceColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinanceColors.current
}

@Composable
fun FinancasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalFinanceColors provides if (darkTheme) DarkFinanceColors else LightFinanceColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography(),
            content = content,
        )
    }
}
