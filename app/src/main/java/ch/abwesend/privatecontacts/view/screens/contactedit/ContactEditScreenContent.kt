package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.view.components.dialogs.EditTextDialog
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactCategory
import ch.abwesend.privatecontacts.view.screens.contactedit.components.EmailAddresses
import ch.abwesend.privatecontacts.view.screens.contactedit.components.PhoneNumbers
import ch.abwesend.privatecontacts.view.screens.contactedit.components.textFieldModifier
import ch.abwesend.privatecontacts.view.util.addOrReplace

@ExperimentalMaterialApi
@Composable
fun ContactEditScreen.ContactEditContent(screenContext: ScreenContext, contact: IContactEditable) {
    val onChanged = { newContact: IContactEditable ->
        screenContext.contactEditViewModel.changeContact(newContact)
    }

    var contactDataWaitingForCustomType: ContactData? by remember { mutableStateOf(null) }
    val customTypeInitialText = contactDataWaitingForCustomType?.type.customValue

    val waitForCustomContactDataType = { contactData: ContactData ->
        if (contactDataWaitingForCustomType != null) {
            logger.warning(
                "overwriting contact data waiting for custom type: " +
                    "from $contactDataWaitingForCustomType to $contactData"
            )
        }
        contactDataWaitingForCustomType = contactData
    }

    val onCustomTypeDefined = { customValue: String ->
        contactDataWaitingForCustomType?.let { contactData ->
            val newContactData = contactData.changeType(ContactDataType.CustomValue(customValue))
            contact.contactDataSet.addOrReplace(newContactData)
            onChanged(contact)
        }
        contactDataWaitingForCustomType = null
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        PersonalInformation(contact, onChanged)
        PhoneNumbers(
            contact = contact,
            waitForCustomType = waitForCustomContactDataType,
            onChanged = onChanged
        )
        EmailAddresses(
            contact = contact,
            waitForCustomType = waitForCustomContactDataType,
            onChanged = onChanged
        )

        Notes(contact, onChanged)
        ContactDataTypeCustomValueDialog(
            visible = contactDataWaitingForCustomType != null,
            initialText = customTypeInitialText,
            hideDialog = { contactDataWaitingForCustomType = null },
            onCustomTypeDefined = onCustomTypeDefined
        )
    }
}

@Composable
private fun ContactEditScreen.PersonalInformation(
    contact: IContactEditable,
    onChanged: (IContactEditable) -> Unit
) {
    ContactCategory(categoryTitle = R.string.personal_information, icon = Icons.Default.Person) {
        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.first_name)) },
                value = contact.firstName,
                onValueChange = { newValue ->
                    contact.firstName = newValue
                    onChanged(contact)
                },
                singleLine = true,
                modifier = textFieldModifier,
            )
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.last_name)) },
                value = contact.lastName,
                onValueChange = { newValue ->
                    contact.lastName = newValue
                    onChanged(contact)
                },
                singleLine = true,
                modifier = textFieldModifier,
            )
        }
    }
}

@Suppress("unused")
@Composable
private fun ContactEditScreen.ContactDataTypeCustomValueDialog(
    visible: Boolean,
    initialText: String,
    hideDialog: () -> Unit,
    onCustomTypeDefined: (String) -> Unit,
) {
    if (visible) {
        EditTextDialog(
            title = R.string.define_custom_type,
            label = R.string.type,
            initialValue = initialText,
            onCancel = hideDialog,
            onSave = onCustomTypeDefined
        )
    }
}

@Composable
private fun ContactEditScreen.Notes(
    contact: IContactEditable,
    onChanged: (IContactEditable) -> Unit
) {
    ContactCategory(categoryTitle = R.string.notes, icon = Icons.Default.SpeakerNotes) {
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.notes)) },
            value = contact.notes,
            onValueChange = { newValue ->
                contact.notes = newValue
                onChanged(contact)
            },
            singleLine = false,
            maxLines = 10,
            modifier = Modifier.heightIn(min = 100.dp)
        )
    }
}

private val ContactDataType?.customValue: String
    get() = (this as? ContactDataType.CustomValue)?.customValue.orEmpty()
