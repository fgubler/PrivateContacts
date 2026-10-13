/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.dialogs.ErrorDialog
import ch.abwesend.privatecontacts.view.components.dialogs.ResourceFlowProgressAndResultDialog
import ch.abwesend.privatecontacts.view.model.ContactGroupWithContactCount
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.model.screencontext.IContactGroupListScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.theme.AppColors

object ContactGroupListScreen {
    @Composable
    fun Screen(screenContext: IContactGroupListScreenContext) {
        val viewModel = screenContext.contactGroupListViewModel
        val settings = screenContext.settings
        var showCreateDialog: Boolean by remember { mutableStateOf(false) }

        // without the existing groups, the uniqueness of a new group's name cannot be validated
        val contactGroupsResource = viewModel.contactGroups.collectAsStateWithLifecycle().value
        val existingGroups = (contactGroupsResource as? ReadyResource)?.value?.map { it.contactGroup }

        LaunchedEffect(Unit) { viewModel.initializeScreen(settings) }

        BaseScreen(
            screenContext = screenContext,
            selectedScreen = Screen.ContactGroups,
            floatingActionButton = {
                if (viewModel.selectedTab.value == ContactGroupListTab.SECRET_GROUPS && existingGroups != null) {
                    AddContactGroupButton(settings) { showCreateDialog = true }
                }
            },
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (settings.invertTopAndBottomBars) {
                    Surface(modifier = Modifier.weight(1.0f)) { // let the tabs get their space first
                        TabContent(viewModel)
                    }
                    TabBox(viewModel, settings)
                } else {
                    TabBox(viewModel, settings)
                    TabContent(viewModel)
                }
            }
        }

        existingGroups?.takeIf { showCreateDialog }?.let { groups ->
            CreateContactGroupDialog(groups, viewModel) { showCreateDialog = false }
        }

        SaveResultHandler(viewModel)
        DeleteResultHandler(viewModel)
    }

    @Composable
    private fun AddContactGroupButton(settings: ISettingsState, onClick: () -> Unit) {
        val verticalOffset = if (settings.invertTopAndBottomBars) -45 else 0 // space for tab-headers
        FloatingActionButton(
            modifier = Modifier.offset(y = verticalOffset.dp),
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.create_contact_group),
            )
        }
    }

    @Composable
    private fun CreateContactGroupDialog(
        existingGroups: List<IContactGroup>,
        viewModel: ContactGroupListViewModel,
        onClose: () -> Unit,
    ) {
        ContactGroupEditDialog(
            title = R.string.new_contact_group_title,
            originalGroup = ContactGroup.new(""),
            allGroups = existingGroups,
            onSave = { contactGroup ->
                onClose()
                viewModel.createContactGroup(contactGroup)
            },
            onCancel = onClose,
        )
    }

    @Composable
    private fun TabBox(viewModel: ContactGroupListViewModel, settings: ISettingsState) {
        if (settings.showAndroidContacts) {
            val selectedTab = viewModel.selectedTab.value

            SecondaryTabRow(selectedTabIndex = selectedTab.index, containerColor = MaterialTheme.colorScheme.surface) {
                ContactGroupListTab.valuesSorted.forEach { tab ->
                    Tab(tab = tab, selectedTab = selectedTab, viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    private fun Tab(tab: ContactGroupListTab, selectedTab: ContactGroupListTab, viewModel: ContactGroupListViewModel) {
        Tab(
            selected = selectedTab == tab,
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { viewModel.selectTab(tab) },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 20.dp),
            ) {
                Icon(imageVector = tab.icon, contentDescription = stringResource(id = tab.label))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = tab.label),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 13.sp,
                )
            }
        }
    }

    @Composable
    private fun TabContent(viewModel: ContactGroupListViewModel) {
        when (viewModel.selectedTab.value) {
            ContactGroupListTab.SECRET_GROUPS -> SecretContactGroups(viewModel)
            ContactGroupListTab.PUBLIC_GROUPS -> FullScreenError(errorMessage = R.string.public_contact_groups_not_implemented)
        }
    }

    @Composable
    private fun SecretContactGroups(viewModel: ContactGroupListViewModel) {
        when (val resource = viewModel.contactGroups.collectAsStateWithLifecycle().value) {
            is ErrorResource -> LoadingError(viewModel)
            is LoadingResource -> LoadingIndicatorFullScreen()
            is InactiveResource -> { /* nothing to show, yet */ }
            is ReadyResource -> {
                if (resource.value.isEmpty()) {
                    FullScreenError(errorMessage = R.string.no_contact_groups_defined)
                } else {
                    ContactGroupList(resource.value, viewModel)
                }
            }
        }
    }

    @Composable
    private fun LoadingError(viewModel: ContactGroupListViewModel) {
        FullScreenError(
            errorMessage = R.string.contact_groups_load_error,
            buttonConfig = ButtonConfig(
                label = R.string.reload_data,
                icon = Icons.Default.Sync
            ) { viewModel.reloadContactGroups() },
        )
    }

    @Composable
    private fun ContactGroupList(contactGroups: List<ContactGroupWithContactCount>, viewModel: ContactGroupListViewModel) {
        var selectedGroup: IContactGroup? by remember { mutableStateOf(null) }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            items(items = contactGroups, key = { it.contactGroup.id.name }) { item ->
                ContactGroupEntry(item) { selectedGroup = item.contactGroup }
            }
            item { Spacer(modifier = Modifier.height(50.dp)) } // padding at the end for the action-button
        }

        selectedGroup?.let { originalGroup ->
            ContactGroupEditDialog(
                title = R.string.edit_contact_group_title,
                originalGroup = originalGroup,
                allGroups = contactGroups.map { it.contactGroup },
                onSave = { newGroup ->
                    selectedGroup = null
                    viewModel.updateContactGroup(originalGroup, newGroup)
                },
                onCancel = { selectedGroup = null },
                onDelete = {
                    selectedGroup = null
                    viewModel.deleteContactGroup(originalGroup)
                },
            )
        }
    }

    @Composable
    private fun ContactGroupEntry(item: ContactGroupWithContactCount, onClick: () -> Unit) {
        val name = item.contactGroup.id.name
        val notes = item.contactGroup.notes

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable(onClick = onClick)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = name,
                modifier = Modifier.padding(start = 10.dp, end = 20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name)
                if (notes.isNotBlank()) {
                    Text(text = notes, color = AppColors.greyText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(text = item.numberOfContacts.toString(), modifier = Modifier.padding(start = 10.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(id = R.string.number_of_contacts),
                modifier = Modifier.padding(start = 5.dp, end = 10.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
    }

    @Composable
    private fun SaveResultHandler(viewModel: ContactGroupListViewModel) {
        ResourceFlowProgressAndResultDialog(
            flow = viewModel.saveResult,
            onCloseDialog = { viewModel.resetSaveResult() },
        ) { result, onClose ->
            when (result) {
                is ContactSaveResult.Success -> LaunchedEffect(Unit) { onClose() }
                is ContactSaveResult.Failure -> ErrorDialog(
                    errorMessage = result.errors.map { stringResource(id = it.label) }.distinct().joinToString("\n"),
                    onClose = onClose,
                )
                is ContactSaveResult.ValidationFailure -> ErrorDialog(
                    errorMessage = stringResource(id = R.string.unable_to_update_contact_group),
                    onClose = onClose,
                )
            }
        }
    }

    @Composable
    private fun DeleteResultHandler(viewModel: ContactGroupListViewModel) {
        ResourceFlowProgressAndResultDialog(
            flow = viewModel.deleteResult,
            onCloseDialog = { viewModel.resetDeleteResult() },
        ) { result, onClose ->
            when (result) {
                is ContactDeleteResult.Success -> LaunchedEffect(Unit) { onClose() }
                is ContactDeleteResult.Failure -> ErrorDialog(
                    errorMessage = result.errors.map { stringResource(id = it.label) }.distinct().joinToString("\n"),
                    onClose = onClose,
                )
            }
        }
    }
}
