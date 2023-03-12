/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil.androidcontacts

import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.IAndroidContactMutableFactory
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
import com.alexstyl.contactstore.Contact

class TestAndroidContactMutableFactory : IAndroidContactMutableFactory {
    override fun toAndroidContactMutable(contact: Contact): IAndroidContactMutable =
        TestAndroidContactMutable(
            contactId = contact.contactId,
            firstName = contact.firstName,
            lastName = contact.lastName,
            middleName = contact.middleName,
            nickname = contact.nickname,
            organization = contact.organization,
            jobTitle = contact.jobTitle,
            prefix = contact.prefix,
            suffix = contact.suffix,
            phoneticLastName = contact.phoneticLastName,
            phoneticFirstName = contact.phoneticFirstName,
            phoneticMiddleName = contact.phoneticMiddleName,
            fullNameStyle = contact.fullNameStyle,
            phoneticNameStyle = contact.phoneticNameStyle,
            phones = contact.phones.toMutableList(),
            mails = contact.mails.toMutableList(),
            events = contact.events.toMutableList(),
            postalAddresses = contact.postalAddresses.toMutableList(),
            webAddresses = contact.webAddresses.toMutableList(),
            imAddresses = contact.imAddresses.toMutableList(),
            sipAddresses = contact.sipAddresses.toMutableList(),
            relations = contact.relations.toMutableList(),
            customDataItems = contact.customDataItems,
            imageData = contact.imageData,
            note = contact.note,
            groups = contact.groups.toMutableList(),
        )

    override fun create(): IAndroidContactMutable = TestAndroidContactMutable(
        contactId = -1,
        firstName = "",
        lastName = "",
        middleName = "",
        nickname = "",
        organization = "",
        jobTitle = "",
        prefix = "",
        suffix = "",
        phoneticLastName = "",
        phoneticFirstName = "",
        phoneticMiddleName = "",
        fullNameStyle = -1,
        phoneticNameStyle = -1,
        phones = mutableListOf(),
        mails = mutableListOf(),
        events = mutableListOf(),
        postalAddresses = mutableListOf(),
        webAddresses = mutableListOf(),
        imAddresses = mutableListOf(),
        sipAddresses = mutableListOf(),
        relations = mutableListOf(),
        customDataItems = mutableListOf(),
        imageData = null,
        note = null,
        groups = mutableListOf(),
    )
}
