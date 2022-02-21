/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.config

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class IconConfig(
    @StringRes val label: Int,
    val icon: ImageVector,
)