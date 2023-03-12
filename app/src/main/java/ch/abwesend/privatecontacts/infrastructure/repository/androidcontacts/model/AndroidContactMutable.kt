/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model

import com.alexstyl.contactstore.ContactColumn
import com.alexstyl.contactstore.CustomDataItem
import com.alexstyl.contactstore.EventDate
import com.alexstyl.contactstore.GroupMembership
import com.alexstyl.contactstore.ImAddress
import com.alexstyl.contactstore.ImageData
import com.alexstyl.contactstore.LabeledValue
import com.alexstyl.contactstore.LookupKey
import com.alexstyl.contactstore.MailAddress
import com.alexstyl.contactstore.MutableContact
import com.alexstyl.contactstore.Note
import com.alexstyl.contactstore.PhoneNumber
import com.alexstyl.contactstore.PostalAddress
import com.alexstyl.contactstore.Relation
import com.alexstyl.contactstore.SipAddress
import com.alexstyl.contactstore.WebAddress

data class AndroidContactMutable(private val mutableContact: MutableContact) : IAndroidContactMutable {
    override val contactId: Long
        get() = mutableContact.contactId

    override var firstName: String
        get() = mutableContact.firstName
        set(value) { mutableContact.firstName = value }

    override var lastName: String
        get() = mutableContact.lastName
        set(value) { mutableContact.lastName = value }

    override var middleName: String
        get() = mutableContact.middleName
        set(value) { mutableContact.middleName = value }

    override var nickname: String
        get() = mutableContact.nickname
        set(value) { mutableContact.nickname = value }

    override var organization: String
        get() = mutableContact.organization
        set(value) { mutableContact.organization = value }

    override var jobTitle: String
        get() = mutableContact.jobTitle
        set(value) { mutableContact.jobTitle = value }

    override var prefix: String
        get() = mutableContact.prefix
        set(value) { mutableContact.prefix = value }

    override var suffix: String
        get() = mutableContact.suffix
        set(value) { mutableContact.suffix = value }

    override var phoneticLastName: String
        get() = mutableContact.phoneticLastName
        set(value) { mutableContact.phoneticLastName = value }

    override var phoneticFirstName: String
        get() = mutableContact.phoneticFirstName
        set(value) { mutableContact.phoneticFirstName = value }

    override var phoneticMiddleName: String
        get() = mutableContact.phoneticMiddleName
        set(value) { mutableContact.phoneticMiddleName = value }

    override var fullNameStyle: Int
        get() = mutableContact.fullNameStyle
        set(value) { mutableContact.fullNameStyle = value }

    override var phoneticNameStyle: Int
        get() = mutableContact.phoneticNameStyle
        set(value) { mutableContact.phoneticNameStyle = value }

    override val phones: MutableList<LabeledValue<PhoneNumber>>
        get() = mutableContact.phones

    override val mails: MutableList<LabeledValue<MailAddress>>
        get() = mutableContact.mails

    override val events: MutableList<LabeledValue<EventDate>>
        get() = mutableContact.events

    override val postalAddresses: MutableList<LabeledValue<PostalAddress>>
        get() = mutableContact.postalAddresses

    override val webAddresses: MutableList<LabeledValue<WebAddress>>
        get() = mutableContact.webAddresses

    override val imAddresses: MutableList<LabeledValue<ImAddress>>
        get() = mutableContact.imAddresses

    override val sipAddresses: MutableList<LabeledValue<SipAddress>>
        get() = mutableContact.sipAddresses

    override val relations: MutableList<LabeledValue<Relation>>
        get() = mutableContact.relations

    override val customDataItems: List<CustomDataItem>
        get() = mutableContact.customDataItems

    override var imageData: ImageData?
        get() = mutableContact.imageData
        set(value) { mutableContact.imageData = value }

    override var note: Note?
        get() = mutableContact.note
        set(value) { mutableContact.note = value }

    override val groups: MutableList<GroupMembership>
        get() = mutableContact.groups

    override val columns: List<ContactColumn>
        get() = mutableContact.columns
    override val displayName: String
        get() = mutableContact.displayName
    override val isStarred: Boolean
        get() = mutableContact.isStarred
    override val lookupKey: LookupKey?
        get() = mutableContact.lookupKey

    override fun toMutableContact(): MutableContact = mutableContact
}
