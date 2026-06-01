/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.model.DropDownOption

@Composable
fun <T> DropDownField(
    @StringRes labelRes: Int,
    value: T,
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
) {
    val selectedOption = options.find { it.value == value }
    DropDownField(
        labelRes = labelRes,
        selectedOption = selectedOption,
        options = options,
        onValueChanged = onValueChanged
    )
}

@Composable
fun <T> DropDownField(
    @StringRes labelRes: Int,
    selectedOption: DropDownOption<T>?,
    options: List<DropDownOption<T>>,
    isError: Boolean = false,
    onValueChanged: (T) -> Unit,
) {
    DropDownComponent(
        options = options,
        onValueChanged = onValueChanged
    ) { _, _ ->
        OutlinedTextField(
            label = { Text(stringResource(id = labelRes)) },
            value = selectedOption?.getLabel().orEmpty(),
            readOnly = true,
            onValueChange = { },
            isError = isError,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            },
        )
    }
}
