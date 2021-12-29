package ch.abwesend.privatecontacts.domain.model

fun ContactBase.getFullName(firstNameFirst: Boolean) =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"
