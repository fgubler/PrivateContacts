/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service

import androidx.annotation.VisibleForTesting
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType

/**
 * Android only allows one "Company" field but the app allows multiple.
 * => translate them into relationships with a fixed type.
 * Should not be translated because otherwise, data would no longer be recognized after changing the language.
 */
@VisibleForTesting
internal const val CUSTOM_RELATIONSHIP_TYPE_ORGANISATION = "Organisation:"

/**
 * Service between the internal data-type "Company" and pseudo-relationships outside of this app.
 */
class AndroidContactCompanyMappingService {
    fun matchesCompanyCustomRelationshipPattern(label: String): Boolean =
        label.startsWith(CUSTOM_RELATIONSHIP_TYPE_ORGANISATION, ignoreCase = true)

    fun encodeToPseudoRelationshipLabel(type: ContactDataType): String {
        val baseLabel = "$CUSTOM_RELATIONSHIP_TYPE_ORGANISATION${type.key.name}"
        return if (type is ContactDataType.CustomValue) "$baseLabel:${type.customValue}"
        else baseLabel
    }

    fun decodeFromPseudoRelationshipLabel(label: String): ContactDataType = try {
        val typeNameWithPotentialCustomValue = label.replaceFirst(
            oldValue = CUSTOM_RELATIONSHIP_TYPE_ORGANISATION,
            newValue = "",
            ignoreCase = true,
        )
        val typeName = typeNameWithPotentialCustomValue.takeWhile { character -> character != ':' }
        val typeKey = ContactDataType.Key.parseOrNull(typeName) ?: ContactDataType.Key.BUSINESS

        val customValue = if (typeKey == ContactDataType.Key.CUSTOM) {
            typeNameWithPotentialCustomValue.replaceFirst(
                oldValue = "$typeName:",
                newValue = "",
                ignoreCase = true,
            ).ifEmpty { null }
        } else null

        ContactDataType.fromKey(key = typeKey, customValue = customValue)
    } catch (e: Exception) {
        logger.error("Failed to map pseudo-relationship to company type", e)
        ContactDataType.Business
    }
}
