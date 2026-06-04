/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val primaryOnLight = Color(0xFF3F51B5)
val primaryOnDark = Color(0xFFC5CAE9)
val primaryVariant = Color(0xFF303F9F)
val secondary = Color(0xFFFF5252)

internal val LightColorScheme = lightColorScheme(
    primary = primaryOnLight,
    primaryContainer = primaryVariant,
    secondary = secondary,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF5F5F5),
    surfaceTint = Color.Transparent,
    onSurfaceVariant = Color(0xFF757575),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F5F5),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEBEBEB),
    surfaceContainerHighest = Color(0xFFE5E5E5),
)

internal val DarkColorScheme = darkColorScheme(
    primary = primaryOnDark,
    primaryContainer = primaryVariant,
    secondary = secondary,
    surfaceVariant = Color(0xFF303030),
    surfaceContainerLowest = Color(0xFF1A1A1A),
    surfaceContainerLow = Color(0xFF222222),
    surfaceContainer = Color(0xFF2A2A2A),
    surfaceContainerHigh = Color(0xFF323232),
    surfaceContainerHighest = Color(0xFF3A3A3A),
)

val ColorScheme.selectedElement: Color
    get() = if (this === LightColorScheme) AppColors.selectedItemOnLight else AppColors.selectedItemOnDark

object AppColors {
    val greyText = Color(0xFF525252)
    val greyBackground = Color(0xFFBBBBBB)
    val selectedItemOnLight = Color(0xFFADD8E6)
    val selectedItemOnDark = Color(0xFF303F9F)
    val goodGreen = Color(0xFF006633)
    val dangerRed = Color(0xFF8b0000)
    val transparent = Color.Transparent
}
