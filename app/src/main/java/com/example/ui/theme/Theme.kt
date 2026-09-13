package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = FastDropBlueLight,
    onPrimary = Color.White,
    primaryContainer = FastDropBlueDark,
    onPrimaryContainer = Color.White,
    secondary = FastDropCyan,
    onSecondary = Color.White,
    secondaryContainer = FastDropBlueDark,
    onSecondaryContainer = Color.White,
    tertiary = FastDropBlue,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = FastDropRed,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FastDropBlue,
    onPrimary = Color.White,
    primaryContainer = FastDropBlueSoft,
    onPrimaryContainer = FastDropBlueDark,
    secondary = FastDropCyan,
    onSecondary = Color.White,
    secondaryContainer = FastDropBlueSubtle,
    onSecondaryContainer = FastDropBlueDark,
    tertiary = FastDropBlueLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = FastDropRed,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

