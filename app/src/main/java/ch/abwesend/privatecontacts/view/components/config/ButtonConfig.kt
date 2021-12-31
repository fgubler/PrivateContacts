package ch.abwesend.privatecontacts.view.components.config

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ButtonConfig(
    @StringRes val label: Int,
    val icon: ImageVector?,
    val onClick: () -> Unit,
)
