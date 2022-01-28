package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.util.getTitle
import ch.abwesend.privatecontacts.view.util.phoneNumbersForDisplay

private val textFieldModifier = Modifier.padding(bottom = 2.dp)
private val contactDataIconModifier = Modifier.padding(top = 23.dp)

@ExperimentalMaterialApi
@Composable
fun ContactEditContent(screenContext: ScreenContext, contact: ContactFull) {
    val onChanged = { newContact: ContactFull -> screenContext.contactEditViewModel.changeContact(newContact) }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        PersonalInformation(contact, onChanged)
        PhoneNumbers(contact, onChanged)

        Notes(contact, onChanged)
    }
}

@Composable
private fun PersonalInformation(contact: ContactFull, onChanged: (ContactFull) -> Unit) {
    ContactCategory(label = R.string.personal_information, icon = Icons.Default.Person) {
        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.first_name)) },
                value = contact.firstName,
                onValueChange = { newValue ->
                    onChanged(contact.copy(firstName = newValue))
                },
                singleLine = true,
                modifier = textFieldModifier,
            )
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.last_name)) },
                value = contact.lastName,
                onValueChange = { newValue ->
                    onChanged(contact.copy(lastName = newValue))
                },
                singleLine = true,
                modifier = textFieldModifier,
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun PhoneNumbers(contact: ContactFull, onChanged: (ContactFull) -> Unit) {
    val onPhoneNumberChanged: (PhoneNumber) -> Unit = { newNumber ->
        val newNumbers =
            if (contact.phoneNumbers.any { it.id == newNumber.id }) {
                contact.phoneNumbers.map {
                    if (it.id == newNumber.id) newNumber
                    else it
                }
            } else {
                contact.phoneNumbers + newNumber
            }
        onChanged(contact.copy(phoneNumbers = newNumbers))
    }

    val phoneNumbersToDisplay = contact.phoneNumbersForDisplay
    ContactCategory(label = R.string.phone_number, icon = Icons.Default.Phone) {
        Column {
            phoneNumbersToDisplay.forEachIndexed { displayIndex, phoneNumber ->
                PhoneNumber(
                    phoneNumber = phoneNumber,
                    isLastElement = (displayIndex == phoneNumbersToDisplay.size - 1),
                    onChanged = onPhoneNumberChanged,
                )
                if (displayIndex < contact.phoneNumbers.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun PhoneNumber(
    phoneNumber: PhoneNumber,
    isLastElement: Boolean,
    onChanged: (PhoneNumber) -> Unit,
) {
    val onPhoneNumberChanged: (String) -> Unit = { newValue ->
        val newStatus = phoneNumber.modelStatus.tryChangeTo(CHANGED)
        val newNumber = phoneNumber.copy(value = newValue, modelStatus = newStatus)
        onChanged(newNumber)
    }
    val onPhoneNumberRemoved: () -> Unit = {
        val newStatus = phoneNumber.modelStatus.tryChangeTo(DELETED)
        val newNumber = phoneNumber.copy(modelStatus = newStatus)
        onChanged(newNumber)
    }

    Row {
        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.phone_number)) },
                value = phoneNumber.value,
                singleLine = true,
                onValueChange = onPhoneNumberChanged,
                modifier = textFieldModifier,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
            )

            ContactDataTypeDropDown(data = phoneNumber) { newType ->
                onChanged(phoneNumber.copy(type = newType))
            }
        }
        if (!isLastElement) {
            IconButton(onClick = onPhoneNumberRemoved) {
                Icon(
                    imageVector = Icons.Default.Close,
                    modifier = contactDataIconModifier,
                    contentDescription = stringResource(id = R.string.remove)
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun ContactDataTypeDropDown(data: ContactData, onChanged: (ContactDataType) -> Unit) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
        modifier = Modifier.widthIn(min = 100.dp, max = 200.dp)
    ) {
        val context = LocalContext.current
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.type)) },
            value = data.type.getTitle(context),
            readOnly = true,
            onValueChange = { }, // read-only...
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
            },
        )
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            data.allowedTypes.forEach { type ->
                DropdownMenuItem(
                    onClick = {
                        onChanged(type)
                        dropdownExpanded = false
                    }
                ) {
                    Text(text = type.getTitle(context))
                }
            }
        }
    }
}

@Composable
private fun Notes(contact: ContactFull, onChanged: (ContactFull) -> Unit) {
    ContactCategory(label = R.string.notes, icon = Icons.Default.SpeakerNotes) {
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.notes)) },
            value = contact.notes,
            onValueChange = { newValue ->
                onChanged(contact.copy(notes = newValue))
            },
            singleLine = false,
            maxLines = 10
        )
    }
}

@Composable
private fun ContactCategory(
    @StringRes label: Int,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = label),
            modifier = contactDataIconModifier.padding(end = 20.dp),
            tint = AppColors.grayText
        )
        content()
    }
}
