/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.settings.Settings

interface IContact : IContactBase {
    val contactDataSet: List<ContactData>
    val firstName: String
    val lastName: String
    val nickname: String
    val notes: String
    val isNew: Boolean

    // TODO this is not very nice (because it accesses the settings): consider not inheriting from IContactBase
    override val displayName: String
        get() {
            val firstNameFirst = Settings.current.orderByFirstName
            return if (firstNameFirst) "$firstName $lastName"
            else "$lastName $firstName"
        }
}

data class Contact(
    override val id: ContactId,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
    override val type: ContactType,
    override val notes: String,
    override val contactDataSet: List<ContactData>,
    override val isNew: Boolean = false,
) : IContact
