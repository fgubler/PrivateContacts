/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import ch.abwesend.privatecontacts.view.model.DropDownOption

/**
 * Component to put a drop-down on any kind of content (not just a text-field)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownComponent(
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
    enabled: Boolean = true,
    content: @Composable (dropDownExpanded: Boolean, modifier: Modifier) -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val closeDropdown = {
        dropdownExpanded = false
        focusManager.clearFocus()
    }

    ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled).fillMaxWidth()) {
            content(dropdownExpanded, Modifier)
        }
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = closeDropdown,
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
