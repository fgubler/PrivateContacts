package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R

@Composable
fun EditTextDialog(
    @StringRes title: Int,
    @StringRes label: Int,
    initialValue: String = "",
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var value: String by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            OutlinedTextField(
                label = { Text(stringResource(id = label)) },
                value = value,
                onValueChange = { newValue -> value = newValue },
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = { onSave(value) }) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
