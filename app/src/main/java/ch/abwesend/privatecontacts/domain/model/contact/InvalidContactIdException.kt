/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

import kotlin.reflect.KClass

class InvalidContactIdException(
    requiredType: KClass<out IContactId>,
    actualType: KClass<out IContactId>
) : IllegalArgumentException(
    "Invalid contact-ID: is of type ${actualType.java.simpleName} " +
        "but should be of type ${requiredType.java.simpleName}"
)

@Deprecated("Cannot be used in tests due to some unboxing issue")
fun IContactBase.requireInternalId(): IContactIdInternal = id.let { contactId ->
    if (contactId !is IContactIdInternal) {
        throw InvalidContactIdException(requiredType = IContactIdInternal::class, actualType = id::class)
    } else contactId
}
