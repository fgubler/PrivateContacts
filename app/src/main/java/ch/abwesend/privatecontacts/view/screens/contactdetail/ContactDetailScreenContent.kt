/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfigGeneric
import ch.abwesend.privatecontacts.view.model.config.IconConfig
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.ContactCategoryWithHeader
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.ContactDataCategory
import ch.abwesend.privatecontacts.view.screens.contactdetail.components.ContactDetailCommonComponents.labelColor
import ch.abwesend.privatecontacts.view.util.color
import ch.abwesend.privatecontacts.view.util.getFullBitmapImage
import ch.abwesend.privatecontacts.view.util.longClickForCopyToClipboard
import ch.abwesend.privatecontacts.view.util.navigateToBrowser
import ch.abwesend.privatecontacts.view.util.navigateToDial
import ch.abwesend.privatecontacts.view.util.navigateToEmailClient
import ch.abwesend.privatecontacts.view.util.navigateToLocation
import ch.abwesend.privatecontacts.view.util.navigateToOnlineSearch
import ch.abwesend.privatecontacts.view.util.navigateToSms
import ch.abwesend.privatecontacts.view.util.navigateToWhatsApp
import ch.abwesend.privatecontacts.view.util.navigateToWhatsApp2

const val UTF_8 = "utf-8"
const val IMAGE_MAX_SIZE_DP = 750

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
object ContactDetailScreenContent {
    @Composable
    fun ScreenContent(contact: IContact, modifier: Modifier = Modifier) {
        val scrollState = rememberScrollState()
        Column(modifier = modifier.verticalScroll(state = scrollState)) {
            PersonalInformation(contact = contact)
            PhoneNumbers(contact = contact)
            EmailAddresses(contact = contact)
            PhysicalAddresses(contact = contact)
            Websites(contact = contact)
            Companies(contact = contact)
            EventDates(contact = contact)
            Relationships(contact = contact)
            ContactGroups(contact = contact)
            Notes(contact = contact)
        }
    }

    @Composable
    private fun PersonalInformation(contact: IContact) {
        ContactCategoryWithHeader(
            categoryTitle = R.string.personal_information,
            icon = Icons.Default.Person,
            alignContentWithTitle = true,
            modifier = Modifier.longClickForCopyToClipboard(contact.displayName)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    if (contact.firstName.isNotEmpty()) {
                        PersonalInformationRow(label = R.string.first_name_colon, value = contact.firstName)
                    }
                    if (contact.lastName.isNotEmpty()) {
                        PersonalInformationRow(label = R.string.last_name_colon, value = contact.lastName)
                    }
                    if (contact.nickname.isNotEmpty()) {
                        PersonalInformationRow(label = R.string.nickname_colon, value = contact.nickname)
                    }
                    if (contact.middleName.isNotEmpty()) {
                        PersonalInformationRow(label = R.string.middle_name_colon, value = contact.middleName)
                    }
                    if (contact.namePrefix.isNotEmpty()) {
                        PersonalInformationRow(label = R.string.name_prefix_colon, value = contact.namePrefix)
                    }
                    if (contact.nameSuffix.isNotEmpty()) {
                        PersonalInformationRow(label = R.string.name_suffix_colon, value = contact.nameSuffix)
                    }

                    PersonalInformationRow(
                        label = R.string.visibility_colon,
                        value = stringResource(id = contact.type.label),
                        valueFontColor = contact.type.color
                    )
                }
                ContactImage(contact = contact)
            }
        }
    }

    @Composable
    private fun PersonalInformationRow(
        @StringRes label: Int,
        value: String,
        valueFontColor: Color = Color.Unspecified
    ) {
        Row {
            Text(text = stringResource(id = label), color = labelColor())
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = value, color = valueFontColor)
        }
    }

    @Composable
    private fun RowScope.ContactImage(contact: IContact) {
        if (!contact.image.isEmpty) {
            contact.getFullBitmapImage(IMAGE_MAX_SIZE_DP)?.let {
                var showFullScreenImage: Boolean by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .weight(1F)
                        .clickable { showFullScreenImage = true }
                ) { ContactImage(it) }

                if (showFullScreenImage) {
                    Dialog(
                        onDismissRequest = { showFullScreenImage = false },
                        content = { ContactImage(it) }
                    )
                }
            }
        }
    }

    @Composable
    private fun ContactImage(bitmap: Bitmap) {
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = stringResource(id = R.string.image))
    }

    @Composable
    private fun PhoneNumbers(contact: IContact) {
        val context = LocalContext.current

        val secondaryActionConfigs = listOf(
            IconButtonConfigGeneric<PhoneNumber>(
                label = R.string.send_whatsapp_message,
                icon = ImageVector.vectorResource(R.drawable.whatsapp_icon)
            ) { phoneNumber -> phoneNumber.navigateToWhatsApp(context) },
            IconButtonConfigGeneric<PhoneNumber>(
                label = R.string.send_sms,
                icon = Icons.Default.Chat
            ) { phoneNumber -> phoneNumber.navigateToSms(context) },
            IconButtonConfigGeneric<PhoneNumber>(
                label = R.string.send_whatsapp_message,
                icon = ImageVector.vectorResource(R.drawable.whatsapp_icon)
            ) { phoneNumber -> phoneNumber.navigateToWhatsApp2(context) },
        )

        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = PhoneNumber.labelSingular, icon = PhoneNumber.icon),
            secondaryActionConfigs = secondaryActionConfigs,
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
    private fun EventDates(contact: IContact) {
        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = EventDate.labelSingular, icon = EventDate.icon),
            factory = { EventDate.createEmpty(it) },
        ) {}
    }

    @Composable
    private fun Relationships(contact: IContact) {
        ContactDataCategory(
            contact = contact,
            iconConfig = IconConfig(label = Relationship.labelSingular, icon = Relationship.icon),
            factory = { Relationship.createEmpty(it) },
        ) {}
    }

    @Composable
    private fun ContactGroups(contact: IContact) {
        if (contact.contactGroups.isNotEmpty()) {
            val groups = remember(contact) {
                contact.contactGroups
                    .map { it.id.name }
                    .filter { it.isNotEmpty() }
                    .sorted()
                    .joinToString(separator = Constants.linebreak)
            }
            ContactCategoryWithHeader(
                categoryTitle = R.string.contact_groups,
                icon = Icons.Default.Groups,
                alignContentWithTitle = true,
                modifier = Modifier.longClickForCopyToClipboard(groups),
            ) {
                Text(text = groups)
            }
        }
    }

    @Composable
    private fun Notes(contact: IContact) {
        if (contact.notes.isNotEmpty()) {
            ContactCategoryWithHeader(
                categoryTitle = R.string.notes,
                icon = Icons.Default.SpeakerNotes,
                alignContentWithTitle = false,
                modifier = Modifier.longClickForCopyToClipboard(contact.notes),
            ) {
                Text(text = contact.notes)
            }
        }
    }
}
