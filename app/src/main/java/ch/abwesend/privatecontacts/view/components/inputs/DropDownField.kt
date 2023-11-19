/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.annotation.StringRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.model.DropDownOption

@ExperimentalMaterialApi
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

@ExperimentalMaterialApi
@Composable
fun <T> DropDownField(
    @StringRes labelRes: Int,
    selectedOption: DropDownOption<T>?,
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
) {
    DropDownComponent(
        options = options,
        onValueChanged = onValueChanged
    ) { dropDownExpanded, modifier ->
        OutlinedTextField(
            label = { Text(stringResource(id = labelRes)) },
            value = selectedOption?.getLabel().orEmpty(),
            readOnly = true,
            onValueChange = { }, // read-only...
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
            },
            modifier = modifier,
        )
    }
}
