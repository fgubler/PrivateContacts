package ch.abwesend.privatecontacts.view.model.config

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Config for an icon which uses the label as content-description
 */
data class IconConfig(
    @StringRes val label: Int,
    val icon: ImageVector,
)