/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.domain.util.simpleClassName

/**
 * If this produces a build-error "when needs to be exhaustive", although it is exhaustive,
 * just run "clean", then it should work
 */
fun ContactData.toEntity(contactId: IContactIdInternal): ContactDataEntity =
    when (this) {
        is StringBasedContactData -> stringBasedToEntity(contactId)
    }

private fun StringBasedContactData.stringBasedToEntity(contactId: IContactIdInternal): ContactDataEntity {
    val internalId: IContactDataIdInternal =
        when (val fixedId = id) {
            is IContactDataIdInternal -> fixedId
            is IContactDataIdExternal -> ContactDataIdInternal.randomId().also {
                logger.warning("Replaced external ID of $simpleClassName with internal one")
            }
        }

    return ContactDataEntity(
        id = internalId.uuid,
        contactId = contactId.uuid,
        category = category,
        type = type.toEntity(),
        sortOrder = sortOrder,
        isMain = isMain,
        valueRaw = value,
        valueFormatted = formattedValue,
        valueForMatching = valueForMatching,
    )
}
