/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.view.components.AddIcon
import ch.abwesend.privatecontacts.view.components.EditIcon
import ch.abwesend.privatecontacts.view.components.dialogs.SaveCancelDialog
import ch.abwesend.privatecontacts.view.components.inputs.DropDownField
import ch.abwesend.privatecontacts.view.model.StringDropDownOption
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.ContactCategory
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import ch.abwesend.privatecontacts.view.util.getTitle
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.contracts.ExperimentalContracts

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalContracts
object EventDateEditComponents {
    private const val LEAP_YEAR_FOR_MAX_DAYS = 2000
    private const val DEFAULT_NUMBER_OF_DAYS = 31

    @Composable
    fun EventDateCategory(
        contact: IContactEditable,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        val dataEntriesToDisplay = remember(contact) {
            contact.contactDataForDisplay(
                addEmptyElement = false,
                factory = { EventDate.createEmpty(it) },
            )
        }

        var editingEntry: EventDate? by remember { mutableStateOf(null) }

        val onEntryChanged: (EventDate) -> Unit = remember(contact) {
            { newEntry ->
                contact.contactDataSet.addOrReplace(newEntry)
                onChanged(contact)
            }
        }

        ContactCategory(
            categoryTitle = EventDate.labelPlural,
            icon = EventDate.icon,
            initiallyExpanded = false,
        ) {
            Column {
                dataEntriesToDisplay.forEach { eventDate ->
                    EventDateRow(
                        eventDate = eventDate,
                        onEdit = { editingEntry = eventDate },
                        onDelete = {
                            contact.contactDataSet.addOrReplace(eventDate.delete())
                            onChanged(contact)
                        },
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }

                TextButton(
                    onClick = { editingEntry = EventDate.createEmpty(dataEntriesToDisplay.size) },
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AddIcon()
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(text = stringResource(R.string.add_event_date))
                        }
                    },
                )
            }
        }

        editingEntry?.let { entry ->
            EventDateEditDialog(
                eventDate = entry,
                waitForCustomType = waitForCustomType,
                onSave = { updatedEntry ->
                    onEntryChanged(updatedEntry)
                    editingEntry = null
                },
                onCancel = { editingEntry = null },
            )
        }
    }

    @Composable
    private fun EventDateRow(
        eventDate: EventDate,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eventDate.displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = eventDate.type.getTitle(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            IconButton(onClick = onEdit) {
                EditIcon()
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                )
            }
        }
    }

    @Composable
    private fun EventDateEditDialog(
        eventDate: EventDate,
        waitForCustomType: (ContactData) -> Unit,
        onSave: (EventDate) -> Unit,
        onCancel: () -> Unit,
    ) {
        val initialDate = eventDate.value

        var selectedDay: Int? by remember(initialDate) { mutableStateOf(initialDate?.dayOfMonth) }
        var selectedMonth: Month? by remember(initialDate) { mutableStateOf(initialDate?.month) }
        var selectedYear: Int? by remember(initialDate) {
            mutableStateOf(if (initialDate != null && eventDate.isYearSet) initialDate.year else null)
        }
        var currentEventDate by remember(eventDate) { mutableStateOf(eventDate) }

        val maxDaysInMonth: Int = remember(selectedMonth, selectedYear) {
            when {
                selectedMonth == null -> DEFAULT_NUMBER_OF_DAYS
                selectedYear != null -> selectedYear?.let {
                    YearMonth.of(it, selectedMonth).lengthOfMonth()
                } ?: DEFAULT_NUMBER_OF_DAYS

                else -> YearMonth.of(LEAP_YEAR_FOR_MAX_DAYS, selectedMonth).lengthOfMonth()
            }
        }
        LaunchedEffect(maxDaysInMonth) {
            if ((selectedDay ?: 0) > maxDaysInMonth) {
                selectedDay = maxDaysInMonth
            }
        }
        val dayOptions: List<StringDropDownOption<Int>> = remember(maxDaysInMonth) {
            (1..maxDaysInMonth).map { day ->
                StringDropDownOption(
                    label = day.toString(),
                    value = day,
                )
            }
        }
        val monthOptions: List<StringDropDownOption<Month>> = remember {
            Month.entries.map { month ->
                StringDropDownOption(
                    label = month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    value = month,
                )
            }
        }
        val noYearLabel = stringResource(R.string.no_year)
        val currentYear = remember { LocalDate.now().year }
        val yearOptions: List<StringDropDownOption<Int?>> = remember(currentYear) {
            val noYearOption = StringDropDownOption<Int?>(label = noYearLabel, value = null)
            val yearEntries = (currentYear downTo 1900).map { year ->
                StringDropDownOption<Int?>(label = year.toString(), value = year)
            }
            listOf(noYearOption) + yearEntries
        }

        val canSave = selectedDay != null && selectedMonth != null

        SaveCancelDialog(
            title = R.string.edit_event_date,
            content = @Composable {
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    DropDownField(
                        labelRes = R.string.day,
                        selectedOption = dayOptions.find { it.value == selectedDay },
                        options = dayOptions,
                        onValueChanged = { selectedDay = it },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DropDownField(
                        labelRes = R.string.month,
                        selectedOption = monthOptions.find { it.value == selectedMonth },
                        options = monthOptions,
                        onValueChanged = { selectedMonth = it },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DropDownField(
                        labelRes = R.string.year_optional,
                        selectedOption = yearOptions.find { it.value == selectedYear },
                        options = yearOptions,
                        onValueChanged = { selectedYear = it },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ContactDataEditCommonComponents.ContactDataTypeDropDown(
                        data = currentEventDate,
                        waitForCustomType = waitForCustomType,
                    ) { newType ->
                        currentEventDate = currentEventDate.changeType(newType)
                    }
                }
            },
            saveButtonEnabled = canSave,
            onSave = {
                val newDate = EventDate.createDate(
                    day = selectedDay,
                    month = selectedMonth?.value,
                    year = selectedYear,
                )
                onSave(currentEventDate.changeValue(newDate))
            },
            onCancel = onCancel,
        )
    }
}
