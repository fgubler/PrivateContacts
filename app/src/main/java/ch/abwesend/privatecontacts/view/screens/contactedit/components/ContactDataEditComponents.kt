/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.KeyboardType
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditCommonComponents.ContactDataCategory

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
object ContactDataEditComponents {

    @Composable
    fun PhoneNumbers(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = PhoneNumber.labelPlural,
            fieldLabel = PhoneNumber.labelSingular,
            icon = PhoneNumber.icon,
            keyboardType = KeyboardType.Phone,
            showIfEmpty = showIfEmpty,
            factory = { PhoneNumber.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun EmailAddresses(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = EmailAddress.labelPlural,
            fieldLabel = EmailAddress.labelSingular,
            icon = EmailAddress.icon,
            keyboardType = KeyboardType.Email,
            showIfEmpty = showIfEmpty,
            factory = { EmailAddress.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun PhysicalAddresses(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = PhysicalAddress.labelPlural,
            fieldLabel = PhysicalAddress.labelSingular,
            icon = PhysicalAddress.icon,
            keyboardType = KeyboardType.Text,
            showIfEmpty = showIfEmpty,
            factory = { PhysicalAddress.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun Websites(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = Website.labelPlural,
            fieldLabel = Website.labelSingular,
            icon = Website.icon,
            keyboardType = KeyboardType.Uri,
            showIfEmpty = showIfEmpty,
            factory = { Website.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun Companies(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = Company.labelPlural,
            fieldLabel = Company.labelSingular,
            icon = Company.icon,
            keyboardType = KeyboardType.Text,
            showIfEmpty = showIfEmpty,
            factory = { Company.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }
}
