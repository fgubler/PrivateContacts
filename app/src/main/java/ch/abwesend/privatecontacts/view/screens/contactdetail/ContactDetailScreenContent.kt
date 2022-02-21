/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
object ContactDetailScreenContent {
    @Composable
    private fun labelColor() = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)

    @Composable
    fun ScreenContent(contact: IContact) {
        Column {
            PersonalInformation(contact = contact)
            PhoneNumbers(contact = contact)
            EmailAddresses(contact = contact)
            PhysicalAddresses(contact = contact)
            Companies(contact = contact)
            Notes(contact = contact)
        }
    }

    @Composable
    private fun PersonalInformation(contact: IContact) {
        ContactDetailCommonComponents.ContactCategoryWithHeader(
            categoryTitle = R.string.personal_information,
            icon = Icons.Default.Person,
            alignContentWithTitle = true,
        ) {
            Row {
                Column {
                    if (contact.firstName.isNotEmpty()) {
                        Text(text = stringResource(id = R.string.first_name_colon), color = labelColor())
                    }
                    if (contact.lastName.isNotEmpty()) {
                        Text(text = stringResource(id = R.string.last_name_colon), color = labelColor())
                    }
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    if (contact.firstName.isNotEmpty()) {
                        Text(text = contact.firstName)
                    }
                    if (contact.lastName.isNotEmpty()) {
                        Text(text = contact.lastName)
                    }
                }
            }
        }
    }

    @Composable
    private fun PhoneNumbers(contact: IContact) {
        // TODO implement

    }

    @Composable
    private fun EmailAddresses(contact: IContact) {
        // TODO implement

    }

    @Composable
    private fun PhysicalAddresses(contact: IContact) {
        // TODO implement

    }

    @Composable
    private fun Companies(contact: IContact) {
        // TODO implement

    }


    @Composable
    private fun Notes(contact: IContact) {
        if (contact.notes.isNotEmpty()) {
            ContactDetailCommonComponents.ContactCategoryWithHeader(
                categoryTitle = R.string.notes,
                icon = Icons.Default.SpeakerNotes,
                alignContentWithTitle = false,
            ) {
                Text(text = contact.notes)
            }
        }
    }
}
