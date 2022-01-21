package ch.abwesend.privatecontacts.view.model.config

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Config for a button which only shows the icon but uses the label as content-description
 */
data class IconButtonConfig(
    @StringRes val label: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
