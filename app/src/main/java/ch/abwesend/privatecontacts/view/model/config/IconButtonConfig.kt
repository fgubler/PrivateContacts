/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model.config

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Config for a button which only shows the icon but uses the label as content-description
 */
interface IIconButtonConfig<T> {
    val label: Int
    val icon: ImageVector
    val onClick: (T) -> Unit
}

data class IconButtonConfigGeneric<T>(
    @StringRes override val label: Int,
    override val icon: ImageVector,
    override val onClick: (T) -> Unit,
) : IIconButtonConfig<T>

typealias IconButtonConfig = IconButtonConfigGeneric<Unit>
