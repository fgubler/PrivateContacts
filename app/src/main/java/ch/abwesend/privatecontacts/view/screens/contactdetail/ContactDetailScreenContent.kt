/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import android.content.Intent
import android.net.Uri
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
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfigGeneric
import ch.abwesend.privatecontacts.view.model.config.IconConfig
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.ContactCategoryWithHeader
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.ContactDataCategory
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.labelColor
import ch.abwesend.privatecontacts.view.util.*
import java.net.URLEncoder

const val UTF_8 = "utf-8"

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
            Websites(contact = contact)
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
        val context = LocalContext.current

        val secondaryActionConfig = IconButtonConfigGeneric<PhoneNumber>(
            label = R.string.send_sms,
            icon = Icons.Default.Chat
        ) { phoneNumber -> phoneNumber.navigateToSms(context) }

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = PhoneNumber.labelSingular, icon = PhoneNumber.icon),
            secondaryActionConfig = secondaryActionConfig,
            factory = { PhoneNumber.createEmpty(it) },
        ) { phoneNumber -> phoneNumber.navigateToDial(context) }
    }

    @Composable
    private fun EmailAddresses(contact: IContact) {
        val context = LocalContext.current

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = EmailAddress.labelSingular, icon = EmailAddress.icon),
            factory = { EmailAddress.createEmpty(it) },
        ) { email -> email.navigateToEmailClient(context) }
    }

    @Composable
    private fun PhysicalAddresses(contact: IContact) {
        val context = LocalContext.current

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = PhysicalAddress.labelSingular, icon = PhysicalAddress.icon),
            factory = { PhysicalAddress.createEmpty(it) },
        ) { location -> location.navigateToLocation(context) }
    }


    @Composable
    private fun Websites(contact: IContact) {
        val context = LocalContext.current

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = Website.labelSingular, icon = Website.icon),
            factory = { Website.createEmpty(it) },
        ) { website -> website.navigateToBrowser(context) }
    }

    @Composable
    private fun Companies(contact: IContact) {
        val context = LocalContext.current

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = Company.labelSingular, icon = Company.icon),
            factory = { Company.createEmpty(it) },
        ) { company -> company.navigateToOnlineSearch(context) }
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
