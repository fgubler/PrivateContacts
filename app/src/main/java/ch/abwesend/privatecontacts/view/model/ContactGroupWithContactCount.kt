/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model

import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup

data class ContactGroupWithContactCount(
    val contactGroup: IContactGroup,
    val numberOfContacts: Int,
)
