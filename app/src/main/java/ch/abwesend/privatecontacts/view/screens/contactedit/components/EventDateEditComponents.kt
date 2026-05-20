/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.view.components.inputs.DropDownField
import ch.abwesend.privatecontacts.view.model.StringDropDownOption
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditCommonComponents.ContactDataTypeDropDown
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.ContactCategory
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.secondaryIconModifier
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.contracts.ExperimentalContracts

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalContracts
object EventDateEditComponents {

    @Composable
    fun EventDateCategory(
        contact: IContactEditable,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
        onValidationChanged: (Boolean) -> Unit = {},
    ) {
        val onEntryChanged: (EventDate) -> Unit = remember(contact) {
            { newEntry ->
                contact.contactDataSet.addOrReplace(newEntry)
                onChanged(contact)
            }
        }

        val dataEntriesToDisplay = remember(contact) {
            contact.contactDataForDisplay(factory = { EventDate.createEmpty(it) })
        }

        val validationByEntryId = remember { mutableStateMapOf<ContactDataId, Boolean>() }
        val hasInvalidEntry = dataEntriesToDisplay.any { validationByEntryId[it.id] == true }
        LaunchedEffect(hasInvalidEntry) {
            onValidationChanged(hasInvalidEntry)
        }

        ContactCategory(
            categoryTitle = EventDate.labelPlural,
            icon = EventDate.icon,
            initiallyExpanded = false,
        ) {
            Column {
                dataEntriesToDisplay.forEachIndexed { index, eventDate ->
                    val isLast = index == dataEntriesToDisplay.size - 1
                    EventDateEntry(
                        eventDate = eventDate,
                        isLastElement = isLast,
                        waitForCustomType = waitForCustomType,
                        onChanged = onEntryChanged,
                        onValidationChanged = { isInvalid -> validationByEntryId[eventDate.id] = isInvalid },
                    )
                    if (!isLast) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun EventDateEntry(
        eventDate: EventDate,
        isLastElement: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (EventDate) -> Unit,
        onValidationChanged: (Boolean) -> Unit,
    ) {
        val initialDate = eventDate.value

        var selectedMonth: Int? by remember(eventDate.id) { mutableStateOf(initialDate?.monthValue) }
        var dayText: String by remember(eventDate.id) {
            mutableStateOf(initialDate?.dayOfMonth?.toString().orEmpty())
        }
        var yearText: String by remember(eventDate.id) {
            val year = if (initialDate != null && eventDate.isYearSet) initialDate.year.toString() else ""
            mutableStateOf(year)
        }

        val monthOptions: List<StringDropDownOption<Int>> = remember {
            Month.entries.map { month ->
                StringDropDownOption(
                    label = month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    value = month.value,
                )
            }
        }

        val selectedMonthOption = remember(selectedMonth) {
            selectedMonth?.let { m -> monthOptions.find { it.value == m } }
        }

        val isDateInvalid = dayText.toIntOrNull() != null && selectedMonth != null &&
            EventDate.createDate(day = dayText.toIntOrNull(), month = selectedMonth, year = yearText.toIntOrNull()) == null
        LaunchedEffect(isDateInvalid) {
            onValidationChanged(isDateInvalid)
        }

        fun updateDate(day: String = dayText, month: Int? = selectedMonth, year: String = yearText) {
            val dayNum = day.toIntOrNull()
            val newDate = EventDate.createDate(day = dayNum, month = month, year = year.toIntOrNull())
            if (dayNum == null || month == null || newDate != null) {
                onChanged(eventDate.changeValue(newDate))
            }
        }

        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        label = { Text(stringResource(R.string.day)) },
                        value = dayText,
                        onValueChange = { input ->
                            dayText = input.filter { it.isDigit() }.take(2)
                            updateDate(day = dayText)
                        },
                        isError = isDateInvalid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DropDownField(
                        labelRes = R.string.month,
                        selectedOption = selectedMonthOption,
                        options = monthOptions,
                        onValueChanged = { newMonth ->
                            selectedMonth = newMonth
                            updateDate(month = newMonth)
                        },
                        isError = isDateInvalid,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        label = { Text(stringResource(R.string.year_optional), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        value = yearText,
                        onValueChange = { input ->
                            yearText = input.filter { it.isDigit() }.take(4)
                            updateDate(year = yearText)
                        },
                        isError = isDateInvalid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (isDateInvalid) {
                        Text(
                            text = stringResource(R.string.invalid_date),
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }

                Box(modifier = secondaryIconModifier) {
                    if (!isLastElement) {
                        IconButton(onClick = { onChanged(eventDate.delete()) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove),
                                modifier = secondaryIconModifier,
                            )
                        }
                    }
                }
            }

            ContactDataTypeDropDown(
                data = eventDate,
                waitForCustomType = waitForCustomType,
            ) { newType -> onChanged(eventDate.changeType(newType)) }
        }
    }
}
