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
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.view.model.config.TextFieldConfig
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditCommonComponents.ContactDataCategory
import kotlin.contracts.ExperimentalContracts

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalContracts
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
            valueFieldConfig = TextFieldConfig(KeyboardType.Phone),
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
            valueFieldConfig = TextFieldConfig(KeyboardType.Email),
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
            valueFieldConfig = TextFieldConfig(minHeight = 80.dp, maxLines = 5),
            showIfEmpty = showIfEmpty,
            factory = { PhysicalAddress.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun Relationships(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = Relationship.labelPlural,
            fieldLabel = Relationship.labelSingular,
            icon = Relationship.icon,
            showIfEmpty = showIfEmpty,
            factory = { Relationship.createEmpty(it) },
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
            valueFieldConfig = TextFieldConfig(KeyboardType.Uri),
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
            showIfEmpty = showIfEmpty,
            factory = { Company.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }
}
