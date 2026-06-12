/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.view.model.DropDownOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownField(
    @StringRes labelRes: Int,
    value: T,
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
) {
    val selectedOption = remember(value, options) { options.find { it.value == value } }
    DropDownField(
        labelRes = labelRes,
        selectedOption = selectedOption,
        options = options,
        onValueChanged = onValueChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownField(
    @StringRes labelRes: Int,
    selectedOption: DropDownOption<T>?,
    options: List<DropDownOption<T>>,
    isError: Boolean = false,
    onValueChanged: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            label = { Text(stringResource(id = labelRes)) },
            value = selectedOption?.getLabel().orEmpty(),
            readOnly = true,
            onValueChange = {}, // read-only...
            isError = isError,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.getLabel()) },
                    onClick = {
                        onValueChanged(option.value)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
