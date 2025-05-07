/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup

enum class ContactGroupValidity {
    VALID,
    EMPTY_NAME,
    DUPLICATE_NAME,
}

@Composable
fun ContactGroupEditComponent(contactGroup: IContactGroup, groupValidity: ContactGroupValidity, onChange: (IContactGroup) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    var dirty: Boolean by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.contact_group_name)) },
            value = contactGroup.id.name,
            singleLine = true,
            onValueChange = { newValue ->
                onChange(contactGroup.changeName(newValue))
                dirty = true
            },
            modifier = Modifier.focusRequester(focusRequester)
        )

        OutlinedTextField(
            label = { Text(stringResource(id = R.string.contact_group_notes)) },
            value = contactGroup.notes,
            onValueChange = { newValue ->
                onChange(contactGroup.changeNotes(newValue))
                dirty = true
            },
        )

        if (dirty && groupValidity != ContactGroupValidity.VALID) {
            Spacer(modifier = Modifier.height(10.dp))
            @Suppress("KotlinConstantConditions")
            val text = when (groupValidity) {
                ContactGroupValidity.VALID -> "ERROR" // cannot happen but the compiler does not know that
                ContactGroupValidity.EMPTY_NAME -> stringResource(id = R.string.contact_group_validation_name_empty)
                ContactGroupValidity.DUPLICATE_NAME -> stringResource(id = R.string.contact_group_validation_name_duplicate)
            }
            Text(text = text, fontStyle = FontStyle.Italic, color = Color.Red)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}