/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SpeakerNotes
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization.Companion.Sentences
import androidx.compose.ui.text.input.KeyboardCapitalization.Companion.Words
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroupId
import ch.abwesend.privatecontacts.view.components.AddIcon
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.EditTextDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SaveCancelDialog
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
import ch.abwesend.privatecontacts.view.util.joinFilteredGroupsToString
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactEditScreenContent {
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
        val hasWritePermission = remember { viewModel.hasContactWritePermission }
        val allContactGroups by viewModel.allContactGroups.collectAsState(initial = InactiveResource())

        Column(
            modifier = modifier.verticalScroll(scrollState)
        ) {
            PersonalInformation(contact, onChanged)
            AdditionalInformation(contact, onChanged)

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

            Groups(contact, onChanged, allContactGroups, viewModel::createContactGroup)
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
            icon = Icons.Default.Person,
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
    private fun AdditionalInformation(
        contact: IContactEditable,
        onChanged: (IContactEditable) -> Unit
    ) {
        val manager = createKeyboardAndFocusManager()

        ContactCategory(
            categoryTitle = R.string.additional_information,
            icon = Icons.Default.PersonAdd,
            initiallyExpanded = false,
        ) {
            Column {
                when (contact.type) {
                    ContactType.PUBLIC -> Unit // cannot edit nickname on public contacts (bug in ContactStore library)
                    ContactType.SECRET -> {
                        OutlinedTextField(
                            label = { Text(stringResource(id = R.string.nickname)) },
                            value = contact.nickname,
                            onValueChange = { newValue ->
                                contact.nickname = newValue
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
                    }
                }

                OutlinedTextField(
                    label = { Text(stringResource(id = R.string.middle_name)) },
                    value = contact.middleName,
                    onValueChange = { newValue ->
                        contact.middleName = newValue
                        onChanged(contact)
                    },
                    singleLine = true,
                    modifier = textFieldModifier,
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = Words),
                    keyboardActions = KeyboardActions(onDone = {
                        manager.closeKeyboardAndClearFocus()
                    }),
                )
                OutlinedTextField(
                    label = { Text(stringResource(id = R.string.name_prefix)) },
                    value = contact.namePrefix,
                    onValueChange = { newValue ->
                        contact.namePrefix = newValue
                        onChanged(contact)
                    },
                    singleLine = true,
                    modifier = textFieldModifier,
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = Words),
                    keyboardActions = KeyboardActions(onDone = {
                        manager.closeKeyboardAndClearFocus()
                    }),
                )
                OutlinedTextField(
                    label = { Text(stringResource(id = R.string.name_suffix)) },
                    value = contact.nameSuffix,
                    onValueChange = { newValue ->
                        contact.nameSuffix = newValue
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
        ContactTypeField(selectedType = contact.type) { newValue ->
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
    private fun Groups(
        contact: IContactEditable,
        onChanged: (IContactEditable) -> Unit,
        allContactGroups: AsyncResource<List<IContactGroup>>,
        onCreateContactGroup: (IContactGroup) -> Unit,
    ) {
        ContactCategory(
            categoryTitle = R.string.contact_groups,
            icon = Icons.Default.Groups,
            initiallyExpanded = false,
            alignContentWithTitle = false,
        ) {
            val emptyText = stringResource(id = R.string.no_contact_groups_set)
            val hasNoGroups = remember(contact) { contact.contactGroups.isEmpty() }

            val groups = remember(contact) { contact.joinFilteredGroupsToString() }
            val text = groups.ifEmpty { emptyText }

            var showDialog by remember { mutableStateOf(false) }

            Row(verticalAlignment = Alignment.CenterVertically)  {
                Text(
                    text = text,
                    fontStyle = FontStyle.Italic.takeIf { hasNoGroups },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                EditIconButton { showDialog = true }
            }

            if (showDialog) {
                var groupsOfContact by remember { mutableStateOf(contact.contactGroups.toList()) }
                val onGroupSelectionChanged: (group: IContactGroup, selected: Boolean) -> Unit =
                    { group, selected ->
                        val otherGroups = groupsOfContact.filterNot { it.id == group.id }
                        val newStatus = if (selected) ModelStatus.NEW else ModelStatus.DELETED
                        val changedGroup = group.changeStatus(newStatus)
                        groupsOfContact = otherGroups + changedGroup
                    }

                SaveCancelDialog(
                    title = R.string.add_contact_to_groups_title,
                    content = @Composable {
                        GroupRelationsEditComponent(
                            allContactGroups = allContactGroups,
                            selectedContactGroupIds = groupsOfContact
                                .filterNot { it.modelStatus == ModelStatus.DELETED }
                                .map { it.id }
                                .toSet(),
                            onCreateContactGroup = { newGroup ->
                                onCreateContactGroup(newGroup)
                                onGroupSelectionChanged(newGroup, true)
                            },
                            onGroupSelectionChanged = onGroupSelectionChanged,
                        )
                    },
                    onCancel = { showDialog = false },
                    onSave = {
                        val contactGroups = groupsOfContact.toList().sortedBy { it.id.name }
                        contact.contactGroups.clear()
                        contact.contactGroups.addAll(contactGroups)
                        onChanged(contact)
                        showDialog = false
                    }
                )
            }
        }
    }

    @Composable
    private fun GroupRelationsEditComponent(
        allContactGroups: AsyncResource<List<IContactGroup>>,
        selectedContactGroupIds: Set<IContactGroupId>,
        onGroupSelectionChanged: (group: IContactGroup, selected: Boolean) -> Unit,
        onCreateContactGroup: (IContactGroup) -> Unit,
    ) {
        when (allContactGroups) {
            is ErrorResource<*>, is InactiveResource<*> -> Text(text = stringResource(R.string.contact_groups_load_error))
            is LoadingResource<*> -> LoadingIndicatorFullScreen()
            is ReadyResource<List<IContactGroup>> -> {
                Column(modifier = Modifier.padding(top = 30.dp, bottom = 10.dp)) {
                    CreateGroupButton(allContactGroups.value, onCreateContactGroup)
                    GroupsList(
                        allContactGroups = allContactGroups.value,
                        selectedContactGroupIds = selectedContactGroupIds,
                        onGroupSelectionChanged = onGroupSelectionChanged,
                    )
                }
            }
        }
    }

    @Composable
    private fun CreateGroupButton(
        existingGroups: Collection<IContactGroup>,
        onCreateContactGroup: (IContactGroup) -> Unit
    ) {
        var showDialog by remember { mutableStateOf(false) }

        TextButton (
            onClick = { showDialog = true },
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AddIcon()
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(text = stringResource(R.string.create_contact_group))
                }
            },
        )

        if (showDialog) {
            var contactGroup: IContactGroup by remember { mutableStateOf(ContactGroup.new("")) }

            val validGroupName = contactGroup.id.name.isNotBlank() &&
                    existingGroups.none { it.id.name.equals(contactGroup.id.name, ignoreCase = true) }

            SaveCancelDialog(
                title = R.string.new_contact_group_title,
                content = @Composable {
                    ContactGroupEditComponent(contactGroup) { contactGroup = it }
                },
                saveButtonEnabled = validGroupName,
                onCancel = { showDialog = false },
                onSave = {
                    onCreateContactGroup(contactGroup)
                    showDialog = false
                }
            )
        }
    }

    @Composable
    private fun ContactGroupEditComponent(contactGroup: IContactGroup, onChange: (IContactGroup) -> Unit) {
        val focusRequester = remember { FocusRequester() }

        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.contact_group_name)) },
                value = contactGroup.id.name,
                onValueChange = { newValue -> onChange(contactGroup.changeName(newValue)) },
                modifier = Modifier.focusRequester(focusRequester)
            )

            OutlinedTextField(
                label = { Text(stringResource(id = R.string.contact_group_notes)) },
                value = contactGroup.notes,
                onValueChange = { newValue -> onChange(contactGroup.changeNotes(newValue)) },
            )
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    @Composable
    private fun GroupsList(
        allContactGroups: List<IContactGroup>,
        selectedContactGroupIds: Set<IContactGroupId>,
        onGroupSelectionChanged: (group: IContactGroup, selected: Boolean) -> Unit,
    ) {
        if (allContactGroups.isEmpty()) {
            Text(text = stringResource(R.string.no_contact_groups_exist))
        } else {
            LazyColumn {
                items(allContactGroups, key = { it.id.name }) { group ->
                    val selected = selectedContactGroupIds.contains(group.id)
                    ContactGroupEntry(group.id.name, selected) {
                        onGroupSelectionChanged(group, !selected)
                    }
                }
            }
        }
    }

    @Composable
    private fun ContactGroupEntry(groupName: String, selected: Boolean, onToggle: () -> Unit) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Icon(Icons.Default.Groups, contentDescription = stringResource(R.string.contact_group))
            Text(text = groupName, modifier = Modifier.weight(1f).padding(start = 20.dp, end = 20.dp))
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
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
            icon = Icons.AutoMirrored.Default.SpeakerNotes,
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
