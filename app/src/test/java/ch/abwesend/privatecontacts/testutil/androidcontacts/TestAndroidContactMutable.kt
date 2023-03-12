/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil.androidcontacts

import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
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
import com.alexstyl.contactstore.allContactColumns

data class TestAndroidContactMutable(
    override val contactId: Long,
    override var firstName: String,
    override var lastName: String,
    override var middleName: String,
    override var nickname: String,
    override var organization: String,
    override var jobTitle: String,
    override var prefix: String,
    override var suffix: String,
    override var phoneticLastName: String,
    override var phoneticFirstName: String,
    override var phoneticMiddleName: String,
    override var fullNameStyle: Int,
    override var phoneticNameStyle: Int,
    override val phones: MutableList<LabeledValue<PhoneNumber>>,
    override val mails: MutableList<LabeledValue<MailAddress>>,
    override val events: MutableList<LabeledValue<EventDate>>,
    override val postalAddresses: MutableList<LabeledValue<PostalAddress>>,
    override val webAddresses: MutableList<LabeledValue<WebAddress>>,
    override val imAddresses: MutableList<LabeledValue<ImAddress>>,
    override val sipAddresses: MutableList<LabeledValue<SipAddress>>,
    override val relations: MutableList<LabeledValue<Relation>>,
    override val customDataItems: List<CustomDataItem>,
    override var imageData: ImageData?,
    override var note: Note?,
    override val groups: MutableList<GroupMembership>
) : IAndroidContactMutable {
    override fun toMutableContact(): MutableContact {
        throw NoSuchMethodError("This is a problem in testing. Should probably not happen.")
    }

    override val columns: List<ContactColumn>
        get() = allContactColumns()

    override val displayName: String
        get() = "$firstName $lastName"

    override val isStarred: Boolean = false
    override val lookupKey: LookupKey? = null
}
