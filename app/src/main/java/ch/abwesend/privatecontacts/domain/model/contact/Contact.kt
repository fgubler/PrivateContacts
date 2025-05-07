/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage

interface IContactBase {
    val id: ContactId
    val type: ContactType
    val displayName: String
}

interface IContact : IContactBase {
    val firstName: String
    val lastName: String
    val nickname: String
    val middleName: String
    val namePrefix: String
    val nameSuffix: String
    val notes: String
    val contactDataSet: List<ContactData>
    val contactGroups: List<IContactGroup>
    val image: ContactImage
    val isNew: Boolean

    /** only relevant for new, external contacts */
    val saveInAccount: ContactAccount

    /** only set for contacts which have been imported (e.g. from VCF) */
    val importId: ContactImportId?
}

data class ContactBase(
    override val id: ContactId,
    override val type: ContactType,
    override val displayName: String,
) : IContactBase
