/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contact.createContactDataId
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EVENT_DATE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Other
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipChild
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipFriend
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipParent
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipPartner
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipRelative
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipSibling
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipWork
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class EventDate(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val value: LocalDate?,
    override val type: ContactDataType,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : GenericContactData<LocalDate?, EventDate> {
    override val category: ContactDataCategory = EVENT_DATE

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override val isEmpty: Boolean = value == null

    override val displayValue: String by lazy {
        value?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)).orEmpty()
    }

    override fun changeValue(value: LocalDate?): EventDate {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): EventDate {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun delete(): EventDate {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    override fun formatValueForSearch(): String = displayValue

    override fun serializedValue(): String =
        value?.format(serializationDateFormatter).orEmpty()

    companion object {
        val icon = Icons.Default.EditCalendar
        const val labelSingular = R.string.event
        const val labelPlural = R.string.events

        private val serializationDateFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ISO_LOCAL_DATE
        }

        private val defaultAllowedTypes = listOf(
            RelationshipParent,
            RelationshipChild,
            RelationshipSibling,
            RelationshipPartner,
            RelationshipFriend,
            RelationshipRelative,
            RelationshipWork,
            Other,
        )

        fun deserializeDate(rawValue: String): LocalDate? = try {
            LocalDate.parse(rawValue, serializationDateFormatter)
        } catch (e: Exception) {
            logger.error("Failed to deserialize event date '$rawValue'", e)
            null
        }

        fun createEmpty(sortOrder: Int): EventDate =
            EventDate(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = Other,
                value = null,
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
