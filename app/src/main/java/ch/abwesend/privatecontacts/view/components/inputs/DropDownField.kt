/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

data class DropDownOption<T>(val label: String, val value: T)

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
    var dropdownExpanded by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val closeDropdown = {
        dropdownExpanded = false
        focusManager.clearFocus()
    }

    ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = {
            // ignore clicks while scrolling: see https://issuetracker.google.com/issues/212091796
            // fallback with focused (in case scrolling should fail at some point)
            dropdownExpanded = !dropdownExpanded && (focused || !isScrolling())
        },
        modifier = Modifier.widthIn(min = 100.dp, max = 200.dp)
    ) {
        OutlinedTextField(
            label = { Text(stringResource(id = labelRes)) },
            value = selectedOption?.label.orEmpty(),
            readOnly = true,
            onValueChange = { }, // read-only...
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
            },
            modifier = Modifier.onFocusChanged { focused = it.isFocused }
        )
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = closeDropdown
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onValueChanged(option.value)
                        closeDropdown()
                    }
                ) {
                    Text(text = option.label)
                }
            }
        }
    }
}
