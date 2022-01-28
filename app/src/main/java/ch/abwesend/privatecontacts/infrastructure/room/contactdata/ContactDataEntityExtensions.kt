package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import java.util.UUID

/**
 * If this produces a build-error "when needs to be exhaustive", although it is exhaustive,
 * just run "clean", then it should work
 */
fun ContactData.toEntity(contactId: UUID): ContactDataEntity =
    when (this) {
        is StringBasedContactData -> toEntity(contactId)
    }

fun PhoneNumber.toEntity(contactId: UUID): ContactDataEntity =
    ContactDataEntity(
        id = id,
        contactId = contactId,
        type = ContactDataType.PHONE_NUMBER,
        subType = type.toEntity(),
        sortOrder = sortOrder,
        isMain = isMain,
        valueRaw = value,
        valueFormatted = formattedValue,
    )
