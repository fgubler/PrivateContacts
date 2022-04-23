/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun normalContentColor(): Color = LocalContentColor.current

@Composable
fun disabledContentColor(): Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current / 2)
