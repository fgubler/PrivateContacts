package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.ContactBase

fun ContactBase.getFullName(firstNameFirst: Boolean) =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"
