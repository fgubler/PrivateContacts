/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import ezvcard.property.Categories

fun Categories.toContactGroup(): ContactGroup? = group?.let {
    val id = ContactGroupId(name = it, groupNo = null)
    ContactGroup(id = id, notes = "", modelStatus = NEW)
}
