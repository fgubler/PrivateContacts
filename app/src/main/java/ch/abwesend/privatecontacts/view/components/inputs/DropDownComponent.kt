/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.model.DropDownOption

/**
 * Component to put a drop-down on any kind of content (not just a text-field)
 */
@ExperimentalMaterialApi
@Composable
fun <T> DropDownComponent(
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
    enabled: Boolean = true,
    maxMenuItemWidth: Dp = Dp.Unspecified,
    content: @Composable (dropDownExpanded: Boolean, modifier: Modifier) -> Unit,
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
            if (enabled) {
                dropdownExpanded = !dropdownExpanded
            }
        },
    ) {
        content(dropdownExpanded, Modifier.onFocusChanged { focused = it.isFocused })
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            modifier = Modifier.widthIn(min = 100.dp, max = maxMenuItemWidth),
            onDismissRequest = closeDropdown,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onValueChanged(option.value)
                        closeDropdown()
                    }
                ) {
                    Text(text = option.getLabel())
                }
            }
        }
    }
}
