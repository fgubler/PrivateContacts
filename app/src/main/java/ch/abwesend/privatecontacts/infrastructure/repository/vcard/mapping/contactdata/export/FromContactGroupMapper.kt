/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ezvcard.property.Categories

fun List<ContactGroup>.toCategories(): Categories {
    val categories = Categories()
    val groupNames = map { it.id.name }
    categories.values.addAll(groupNames)
    return categories
}
