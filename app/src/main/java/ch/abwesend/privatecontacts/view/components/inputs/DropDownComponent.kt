/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.model.DropDownOption

@Composable
fun <T> DropDownComponent(
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
    enabled: Boolean = true,
    maxMenuItemWidth: Dp = Dp.Unspecified,
    content: @Composable (dropDownExpanded: Boolean, modifier: Modifier) -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val closeDropdown = {
        dropdownExpanded = false
        focusManager.clearFocus()
    }

    Box(modifier = if (enabled) Modifier.clickable { dropdownExpanded = true } else Modifier) {
        content(dropdownExpanded, Modifier)
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = closeDropdown,
            modifier = Modifier.widthIn(min = 100.dp, max = maxMenuItemWidth),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.getLabel()) },
                    onClick = {
                        onValueChanged(option.value)
                        closeDropdown()
                    }
                )
            }
        }
    }
}
