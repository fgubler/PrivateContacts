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
    isScrolling: () -> Boolean,
    onValueChanged: (T) -> Unit,
) {
    val selectedOption = options.find { it.value == value }
    DropDownField(
        labelRes = labelRes,
        selectedOption = selectedOption,
        options = options,
        isScrolling = isScrolling,
        onValueChanged = onValueChanged
    )
}
/**
 * [isScrolling] work-around for a bug: ignore clicks while scrolling.
 */
@ExperimentalMaterialApi
@Composable
fun <T> DropDownField(
    @StringRes labelRes: Int,
    selectedOption: DropDownOption<T>?,
    options: List<DropDownOption<T>>,
    isScrolling: () -> Boolean,
    onValueChanged: (T) -> Unit,
) {
    DropDownComponent(
        selectedOption = selectedOption,
        options = options,
        isScrolling = isScrolling,
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
