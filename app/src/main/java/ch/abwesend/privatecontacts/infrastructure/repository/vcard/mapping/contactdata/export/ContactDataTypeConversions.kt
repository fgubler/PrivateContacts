/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.RelatedType
import ezvcard.parameter.TelephoneType

fun Company.getRelatedType(typeString: String): RelatedType? =
    getVCardDataType(typeString) { RelatedType.get(it) }

fun EmailAddress.getEmailType(typeString: String): EmailType? =
    getVCardDataType(typeString) { EmailType.get(it) }

fun PhoneNumber.getTelephoneType(typeString: String): TelephoneType? =
    getVCardDataType(typeString) { TelephoneType.get(it) }

fun PhysicalAddress.getAddressType(typeString: String): AddressType? =
    getVCardDataType(typeString) { AddressType.get(it) }

fun Relationship.getRelatedType(typeString: String): RelatedType? =
    getVCardDataType(typeString) { RelatedType.get(it) }

private inline fun <reified T : Any, S : Any> S.getVCardDataType(
    typeString: String,
    converter: (String) -> T,
): T? = try {
    converter(typeString)
} catch (e: Exception) {
    val baseMessage = "Failed to get ${T::class.java.simpleName}"
    logger.error(baseMessage, e) // this would probably happen due to Proguard and Reflection => need to know...
    logger.debugLocally("$baseMessage '$typeString'", e)
    null
}
