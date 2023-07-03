/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit

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
import androidx.compose.material.icons.filled.Visibility
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
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.view.components.dialogs.EditTextDialog
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.Companies
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.EmailAddresses
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.PhoneNumbers
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.PhysicalAddresses
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.Relationships
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditComponents.Websites
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.ContactCategory
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.textFieldModifier
import ch.abwesend.privatecontacts.view.util.accountSelectionRequired
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.bringIntoViewDelayed
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactEditScreenContent {
    private val parent = ContactEditScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ContactEditContent(
        viewModel: ContactEditViewModel,
        contact: IContactEditable,
        modifier: Modifier = Modifier,
    ) {
        val onChanged = { newContact: IContactEditable ->
            viewModel.changeContact(newContact)
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
        val hasWritePermission = remember { viewModel.hasContactWritePermission }

        Column(
            modifier = modifier.verticalScroll(scrollState)
        ) {
            PersonalInformation(contact, onChanged)

            if (contact.isNew && hasWritePermission) {
                Visibility(contact = contact, onChanged = onChanged)
            }

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
            PhysicalAddresses(
                contact = contact,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            Relationships(
                contact = contact,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            Websites(
                contact = contact,
                waitForCustomType = waitForCustomContactDataType,
                onChanged = onChanged
            )
            Companies(
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
    private fun Visibility(contact: IContactEditable, onChanged: (IContactEditable) -> Unit) {
        ContactCategory(
            categoryTitle = R.string.visibility,
            icon = Icons.Default.Visibility,
            initiallyExpanded = contact.isNew,
            alignContentWithTitle = true,
        ) {
            Column {
                ContactTypeField(contact, onChanged)
                AccountSelectionField(contact, onChanged)
            }
        }
    }

    @Composable
    private fun ContactTypeField(contact: IContactEditable, onChanged: (IContactEditable) -> Unit) {
        ContactTypeField(
            selectedType = contact.type,
            isScrolling = { parent.isScrolling },
        ) { newValue ->
            val oldValue = contact.type
            contact.type = newValue
            adaptSaveInAccount(contact, oldValue, newValue)
            onChanged(contact)
        }
    }

    private fun adaptSaveInAccount(contact: IContactEditable, oldType: ContactType, newType: ContactType) {
        if (oldType != newType) {
            val newAccount = ContactAccount.currentDefaultForContactType(newType)
            contact.saveInAccount = newAccount
        }
    }

    @Composable
    private fun AccountSelectionField(contact: IContactEditable, onChanged: (IContactEditable) -> Unit) {
        if (contact.type.accountSelectionRequired) {
            AccountSelectionDropDownField(selectedAccount = contact.saveInAccount) { newValue ->
                contact.saveInAccount = newValue
                onChanged(contact)
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
