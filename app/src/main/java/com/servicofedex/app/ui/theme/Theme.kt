package com.servicofedex.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Todos os papéis de cor são preenchidos explicitamente para que o Material 3
 * não introduce cores padrão (branco/preto) fora da paleta autorizada.
 */
private val DarkColors: ColorScheme = darkColorScheme(
    primary = Yellow,
    onPrimary = Dark,
    primaryContainer = YellowDeep,
    onPrimaryContainer = Dark,
    inversePrimary = YellowDeep,
    secondary = YellowDeep,
    onSecondary = Dark,
    secondaryContainer = Surface,
    onSecondaryContainer = Yellow,
    tertiary = Yellow,
    onTertiary = Dark,
    tertiaryContainer = Surface,
    onTertiaryContainer = YellowDeep,
    background = Dark,
    onBackground = Yellow,
    surface = Surface,
    onSurface = Yellow,
    surfaceVariant = Dark,
    onSurfaceVariant = YellowDeep,
    surfaceTint = Yellow,
    inverseSurface = YellowDeep,
    inverseOnSurface = Dark,
    error = YellowDeep,
    onError = Dark,
    errorContainer = Surface,
    onErrorContainer = Yellow,
    outline = YellowDeep,
    outlineVariant = Surface,
    scrim = Dark,
    surfaceBright = Surface,
    surfaceDim = Dark,
    surfaceContainer = Surface,
    surfaceContainerHigh = Dark,
    surfaceContainerHighest = Surface,
    surfaceContainerLow = Dark,
    surfaceContainerLowest = Dark,
)

private val LightColors: ColorScheme = lightColorScheme(
    primary = Dark,
    onPrimary = Yellow,
    primaryContainer = Yellow,
    onPrimaryContainer = Dark,
    inversePrimary = Surface,
    secondary = Surface,
    onSecondary = Yellow,
    secondaryContainer = YellowDeep,
    onSecondaryContainer = Dark,
    tertiary = Dark,
    onTertiary = Yellow,
    tertiaryContainer = Yellow,
    onTertiaryContainer = Dark,
    background = YellowDeep,
    onBackground = Dark,
    surface = Yellow,
    onSurface = Dark,
    surfaceVariant = YellowDeep,
    onSurfaceVariant = Surface,
    surfaceTint = Dark,
    inverseSurface = Dark,
    inverseOnSurface = Yellow,
    error = Dark,
    onError = Yellow,
    errorContainer = Yellow,
    onErrorContainer = Dark,
    outline = Surface,
    outlineVariant = YellowDeep,
    scrim = Dark,
    surfaceBright = Yellow,
    surfaceDim = YellowDeep,
    surfaceContainer = Yellow,
    surfaceContainerHigh = YellowDeep,
    surfaceContainerHighest = Yellow,
    surfaceContainerLow = YellowDeep,
    surfaceContainerLowest = YellowDeep,
)

@Composable
fun ServicoFedexTheme(
    darkTheme: Boolean = !isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
