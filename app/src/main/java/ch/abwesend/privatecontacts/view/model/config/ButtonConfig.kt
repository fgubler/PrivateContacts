package ch.abwesend.privatecontacts.view.model.config

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Config for a button which shows a text and maybe also an icon
 */
data class ButtonConfig(
    @StringRes val label: Int,
    val icon: ImageVector?,
    val onClick: () -> Unit,
)
