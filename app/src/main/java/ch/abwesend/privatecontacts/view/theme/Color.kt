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
)

internal val DarkColorScheme = darkColorScheme(
    primary = primaryOnDark,
    primaryContainer = primaryVariant,
    secondary = secondary,
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
