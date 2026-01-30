/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hawkfranklin.aura.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.hawkfranklin.aura.proto.Theme

private val lightScheme =
  lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
  )

private val darkScheme =
  darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
  )

@Immutable
data class CustomColors(
  val appTitleGradientColors: List<Color> = listOf(),
  val tabHeaderBgColor: Color = Color.Transparent,
  val taskCardBgColor: Color = Color.Transparent,
  val taskBgColors: List<Color> = listOf(),
  val taskBgGradientColors: List<List<Color>> = listOf(),
  val taskIconColors: List<Color> = listOf(),
  val taskIconShapeBgColor: Color = Color.Transparent,
  val homeBottomGradient: List<Color> = listOf(),
  val userBubbleBgColor: Color = Color.Transparent,
  val agentBubbleBgColor: Color = Color.Transparent,
  val linkColor: Color = Color.Transparent,
  val successColor: Color = Color.Transparent,
  val recordButtonBgColor: Color = Color.Transparent,
  val waveFormBgColor: Color = Color.Transparent,
  val modelInfoIconColor: Color = Color.Transparent,
  val warningContainerColor: Color = Color.Transparent,
  val warningTextColor: Color = Color.Transparent,
  val errorContainerColor: Color = Color.Transparent,
  val errorTextColor: Color = Color.Transparent,
)

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }

val lightCustomColors =
  CustomColors(
    appTitleGradientColors =
      listOf(Color(0xFFD9A441), Color(0xFFC5A028), Color(0xFFA3B1C6), Color(0xFF22D3EE)),
    tabHeaderBgColor = Color(0xFFD9A441),
    taskCardBgColor = surfaceContainerLowestLight,
    taskBgColors =
      listOf(
        Color(0xFFFFFBF0),
        Color(0xFFF2FAFB),
        Color(0xFFF4F6FA),
        Color(0xFFFFF7E6),
      ),
    taskBgGradientColors =
      listOf(
        listOf(Color(0xFFD9A441), Color(0xFFC5A028)),
        listOf(Color(0xFF22D3EE), Color(0xFF0FB3CC)),
        listOf(Color(0xFFA3B1C6), Color(0xFF7B8AA3)),
        listOf(Color(0xFFEACB7A), Color(0xFFD2A94F)),
      ),
    taskIconColors =
      listOf(
        Color(0xFFD9A441),
        Color(0xFF22D3EE),
        Color(0xFFA3B1C6),
        Color(0xFFC5A028),
      ),
    taskIconShapeBgColor = Color.White,
    homeBottomGradient = listOf(Color(0x00F8F9FF), Color(0x66D9A441)),
    agentBubbleBgColor = Color(0xFFF1F2F4),
    userBubbleBgColor = Color(0xFFD9A441),
    linkColor = Color(0xFF0FB3CC),
    successColor = Color(0xFF2E7D32),
    recordButtonBgColor = Color(0xFF22D3EE),
    waveFormBgColor = Color(0xFF9CA3AF),
    modelInfoIconColor = Color(0xFFB6BBC4),
    warningContainerColor = Color(0xFFFFF3D6),
    warningTextColor = Color(0xFFB45309),
    errorContainerColor = Color(0xFFFCE8E6),
    errorTextColor = Color(0xFFB3261E),
  )

val darkCustomColors =
  CustomColors(
    appTitleGradientColors =
      listOf(Color(0xFFD9A441), Color(0xFFC5A028), Color(0xFFA3B1C6), Color(0xFF22D3EE)),
    tabHeaderBgColor = Color(0xFF1B2336),
    taskCardBgColor = surfaceContainerHighDark,
    taskBgColors =
      listOf(
        Color(0xFF141318),
        Color(0xFF101820),
        Color(0xFF141A24),
        Color(0xFF17140F),
      ),
    taskBgGradientColors =
      listOf(
        listOf(Color(0xFFD9A441), Color(0xFFC5A028)),
        listOf(Color(0xFF22D3EE), Color(0xFF0FB3CC)),
        listOf(Color(0xFFA3B1C6), Color(0xFF7B8AA3)),
        listOf(Color(0xFFEACB7A), Color(0xFFD2A94F)),
      ),
    taskIconColors =
      listOf(
        Color(0xFFD9A441),
        Color(0xFF22D3EE),
        Color(0xFFA3B1C6),
        Color(0xFFC5A028),
      ),
    taskIconShapeBgColor = Color(0xFF1B2336),
    homeBottomGradient = listOf(Color(0x00020617), Color(0x33D9A441)),
    agentBubbleBgColor = Color(0xFF0F172A),
    userBubbleBgColor = Color(0xFF1F2937),
    linkColor = Color(0xFF7DD3FC),
    successColor = Color(0xFFA1CE83),
    recordButtonBgColor = Color(0xFF22D3EE),
    waveFormBgColor = Color(0xFF64748B),
    modelInfoIconColor = Color(0xFF9CA3AF),
    warningContainerColor = Color(0xFF3A2B12),
    warningTextColor = Color(0xFFFBBF24),
    errorContainerColor = Color(0xFF3B1F23),
    errorTextColor = Color(0xFFFCA5A5),
  )

val MaterialTheme.customColors: CustomColors
  @Composable @ReadOnlyComposable get() = LocalCustomColors.current

/**
 * Controls the color of the phone's status bar icons based on whether the app is using a dark
 * theme.
 */
@Composable
fun StatusBarColorController(useDarkTheme: Boolean) {
  val view = LocalView.current
  val currentWindow = (view.context as? Activity)?.window

  if (currentWindow != null) {
    SideEffect {
      WindowCompat.setDecorFitsSystemWindows(currentWindow, false)
      val controller = WindowCompat.getInsetsController(currentWindow, view)
      controller.isAppearanceLightStatusBars = !useDarkTheme // Set to true for light icons
    }
  }
}

@Composable
fun GalleryTheme(content: @Composable () -> Unit) {
  val themeOverride = ThemeSettings.themeOverride
  val darkTheme: Boolean =
    (isSystemInDarkTheme() || themeOverride.value == Theme.THEME_DARK) &&
      themeOverride.value != Theme.THEME_LIGHT

  StatusBarColorController(useDarkTheme = darkTheme)

  val colorScheme =
    when {
      darkTheme -> darkScheme
      else -> lightScheme
    }

  val customColorsPalette = if (darkTheme) darkCustomColors else lightCustomColors

  CompositionLocalProvider(LocalCustomColors provides customColorsPalette) {
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
  }
}
