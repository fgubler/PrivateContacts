/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization.Companion.Sentences
import androidx.compose.ui.text.input.KeyboardCapitalization.Companion.Words
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.view.components.dialogs.EditTextDialog
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.Companies
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.EmailAddresses
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.PhoneNumbers
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.PhysicalAddresses
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.Websites
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.ContactCategory
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.textFieldModifier
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.bringIntoViewDelayed
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactEditScreenContent {
    private val parent = ContactEditScreen

    @Composable
    fun ContactEditContent(
        screenContext: ScreenContext,
        contact: IContactEditable,
        showAllFields: Boolean,
        modifier: Modifier = Modifier,
    ) {
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
                val newContactData =
                    contactData.changeType(ContactDataType.CustomValue(customValue))
                contact.contactDataSet.addOrReplace(newContactData)
                onChanged(contact)
            }
            contactDataWaitingForCustomType = null
        }

        val scrollState = rememberScrollState()
        parent.isScrolling = scrollState.isScrollInProgress

        Column(
            modifier = modifier.verticalScroll(scrollState)
        ) {
            PersonalInformation(contact, onChanged)
            PhoneNumbers(
                contact = contact,
                showIfEmpty = showAllFields,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            EmailAddresses(
                contact = contact,
                showIfEmpty = showAllFields,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            PhysicalAddresses(
                contact = contact,
                showIfEmpty = showAllFields,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            Websites(
                contact = contact,
                showIfEmpty = showAllFields,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            Companies(
                contact = contact,
                showIfEmpty = showAllFields,
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
    private fun PersonalInformation(
        contact: IContactEditable,
        onChanged: (IContactEditable) -> Unit
    ) {
        val manager = createKeyboardAndFocusManager()

        ContactCategory(
            categoryTitle = R.string.personal_information,
            icon = Icons.Default.Person
        ) {
            Column {
                OutlinedTextField(
                    label = { Text(stringResource(id = R.string.first_name)) },
                    value = contact.firstName,
                    onValueChange = { newValue ->
                        contact.firstName = newValue
                        onChanged(contact)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        capitalization = Words
                    ),
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
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = Words),
                    keyboardActions = KeyboardActions(onDone = {
                        manager.closeKeyboardAndClearFocus()
                    }),
                )
            }
        }
    }

    @Composable
    private fun ContactDataTypeCustomValueDialog(
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
        BackHandler(enabled = visible) { hideDialog() }
    }

    @Composable
    private fun Notes(
        contact: IContactEditable,
        onChanged: (IContactEditable) -> Unit
    ) {
        val viewRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()

        ContactCategory(
            categoryTitle = R.string.notes,
            icon = Icons.Default.SpeakerNotes,
            modifier = Modifier.bringIntoViewRequester(viewRequester),
            alignContentWithTitle = false,
        ) {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.notes)) },
                value = contact.notes,
                onValueChange = { newValue ->
                    contact.notes = newValue
                    onChanged(contact)
                },
                singleLine = false,
                maxLines = 10,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = Sentences),
                modifier = Modifier
                    .heightIn(min = 100.dp)
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) {
                            coroutineScope.launch {
                                viewRequester.bringIntoViewDelayed()
                            }
                        }
                    }
            )
        }
    }

    private val ContactDataType?.customValue: String
        get() = (this as? ContactDataType.CustomValue)?.customValue.orEmpty()
}
