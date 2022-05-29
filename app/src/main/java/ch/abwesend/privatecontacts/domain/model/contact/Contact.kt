/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData

interface IContact : IContactBase {
    val contactDataSet: List<ContactData>
    val nickname: String
    val notes: String
    val isNew: Boolean
}
