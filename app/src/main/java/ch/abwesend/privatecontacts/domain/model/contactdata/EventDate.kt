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
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory.EVENT_DATE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Birthday
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Custom
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Other
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

/**
 * The year 0 is set if no year was entered
 */
private const val DUMMY_YEAR_STRING = "0000"
private const val DUMMY_YEAR_INT = 0

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
            .replace(oldValue = DUMMY_YEAR_STRING, newValue = "")
    }

    override fun overrideStatus(newStatus: ModelStatus): ContactData = copy(modelStatus = newStatus)
    override fun changeToInternalId(): ContactData = copy(id = createContactDataId())
    override fun changeToExternalId(): ContactData = copy(id = createExternalDummyContactDataId())

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
            Birthday,
            Custom,
            Other,
        )

        fun deserializeDate(rawValue: String): LocalDate? = try {
            LocalDate.parse(rawValue, serializationDateFormatter)
        } catch (e: DateTimeParseException) {
            logger.error("Failed to deserialize event date '$rawValue'", e)
            null
        }

        fun createDate(day: Int, month: Int, year: Int?): LocalDate? = try {
            val yearOrDummy: Int = year ?: DUMMY_YEAR_INT
            LocalDate.of(yearOrDummy, month, day)
        } catch (e: DateTimeException) {
            logger.error("Failed to create date from day = $day, month = $month, year = $year", e)
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
