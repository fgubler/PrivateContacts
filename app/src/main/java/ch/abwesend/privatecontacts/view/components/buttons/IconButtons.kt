package ch.abwesend.privatecontacts.view.components.buttons

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.view.components.BackIcon
import ch.abwesend.privatecontacts.view.components.CancelIcon
import ch.abwesend.privatecontacts.view.components.SaveIcon

@Composable
fun SaveIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) { SaveIcon() }
}

@Composable
fun CancelIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) { CancelIcon() }
}

@Composable
fun BackIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) { BackIcon() }
}
