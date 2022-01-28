package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactDataSimple
import java.util.UUID

/**
 * If this produces a build-error "when needs to be exhaustive", although it is exhaustive,
 * just run "clean", then it should work
 */
fun ContactData.toEntity(contactId: UUID): ContactDataEntity =
    when (this) {
        is StringBasedContactDataSimple -> stringBasedToEntity(contactId)
    }

private fun StringBasedContactDataSimple.stringBasedToEntity(contactId: UUID): ContactDataEntity =
    ContactDataEntity(
        id = id,
        contactId = contactId,
        category = ContactDataCategory.PHONE_NUMBER,
        type = type.toEntity(),
        sortOrder = sortOrder,
        isMain = isMain,
        valueRaw = value,
        valueFormatted = formattedValue,
    )
