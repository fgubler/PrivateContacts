/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfig
import ch.abwesend.privatecontacts.view.model.config.IconConfig
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.ContactCategoryWithHeader
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.ContactDataCategory
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.labelColor

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
object ContactDetailScreenContent {
    @Composable
    fun ScreenContent(contact: IContact) {
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.verticalScroll(state = scrollState)) {
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
        ContactCategoryWithHeader(
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
        val context = LocalContext.current // TODO remove

        val secondaryActionConfig = IconButtonConfig(
            label = R.string.send_sms,
            icon = Icons.Default.Chat
        ) {
            // TODO implement
            Toast.makeText(context, "Send SMS", Toast.LENGTH_SHORT).show()
        }

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = PhoneNumber.labelSingular, icon = PhoneNumber.icon),
            secondaryActionConfig = secondaryActionConfig,
            factory = { PhoneNumber.createEmpty(it) },
        ) {
            // TODO implement
            Toast.makeText(context, "Call Contact", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun EmailAddresses(contact: IContact) {
        val context = LocalContext.current // TODO remove

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = EmailAddress.labelSingular, icon = EmailAddress.icon),
            factory = { EmailAddress.createEmpty(it) },
        ) {
            // TODO implement
            Toast.makeText(context, "Send Email", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun PhysicalAddresses(contact: IContact) {
        val context = LocalContext.current // TODO remove

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = PhysicalAddress.labelSingular, icon = PhysicalAddress.icon),
            factory = { PhysicalAddress.createEmpty(it) },
        ) {
            // TODO implement
            Toast.makeText(context, "Find location", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun Companies(contact: IContact) {
        val context = LocalContext.current // TODO remove

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = Company.labelSingular, icon = Company.icon),
            factory = { Company.createEmpty(it) },
        ) {
            // TODO implement
            Toast.makeText(context, "Search for company", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    private fun Notes(contact: IContact) {
        if (contact.notes.isNotEmpty()) {
            ContactCategoryWithHeader(
                categoryTitle = R.string.notes,
                icon = Icons.Default.SpeakerNotes,
                alignContentWithTitle = false,
            ) {
                Text(text = contact.notes)
            }
        }
    }
}
