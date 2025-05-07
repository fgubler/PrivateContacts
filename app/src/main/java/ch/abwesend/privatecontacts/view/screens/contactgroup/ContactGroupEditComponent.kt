/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.compose.foundation.layout.Column
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup

@Composable
fun ContactGroupEditComponent(contactGroup: IContactGroup, onChange: (IContactGroup) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    Column {
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.contact_group_name)) },
            value = contactGroup.id.name,
            onValueChange = { newValue -> onChange(contactGroup.changeName(newValue)) },
            modifier = Modifier.focusRequester(focusRequester)
        )

        OutlinedTextField(
            label = { Text(stringResource(id = R.string.contact_group_notes)) },
            value = contactGroup.notes,
            onValueChange = { newValue -> onChange(contactGroup.changeNotes(newValue)) },
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}