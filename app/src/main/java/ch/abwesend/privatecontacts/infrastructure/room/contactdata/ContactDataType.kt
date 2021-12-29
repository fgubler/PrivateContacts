package ch.abwesend.privatecontacts.infrastructure.room.contactdata

import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubType.BIRTHDAY
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubType.CUSTOM
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubType.MOBILE
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubType.OTHER
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubType.PRIVATE
import ch.abwesend.privatecontacts.infrastructure.room.contactdata.ContactDataSubType.PUBLIC

enum class ContactDataType(vararg subtypes: ContactDataSubType) {
    EMAIL(PRIVATE, PUBLIC, OTHER, CUSTOM),
    PHONE(MOBILE, PRIVATE, PUBLIC, OTHER, CUSTOM),
    ADDRESS(PRIVATE, PUBLIC, OTHER, CUSTOM),
    WEBSITE,
    DATE(BIRTHDAY),
}

enum class ContactDataSubType {
    PRIVATE,
    PUBLIC,
    MOBILE,
    OTHER,
    CUSTOM,
    BIRTHDAY,
}
