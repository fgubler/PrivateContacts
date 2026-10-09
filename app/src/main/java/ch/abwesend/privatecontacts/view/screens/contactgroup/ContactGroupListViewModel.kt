/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.emitInactive
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.ContactGroupService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.model.ContactGroupWithContactCount
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactGroupListViewModel : ViewModel() {
    private val contactGroupService: ContactGroupService by injectAnywhere()

    private val _selectedTab = mutableStateOf(ContactGroupListTab.default)
    val selectedTab: State<ContactGroupListTab> = _selectedTab

    private var loadingJob: Job? = null

    private val _contactGroups = mutableResourceStateFlow<List<ContactGroupWithContactCount>>()
    val contactGroups: ResourceStateFlow<List<ContactGroupWithContactCount>> = _contactGroups.asStateFlow()

    /** implemented as a resource to show a loading-indicator during saving */
    private val _saveResult = mutableResourceStateFlow<ContactSaveResult>()
    val saveResult: ResourceFlow<ContactSaveResult> = _saveResult

    /** implemented as a resource to show a loading-indicator during deletion */
    private val _deleteResult = mutableResourceStateFlow<ContactDeleteResult>()
    val deleteResult: ResourceFlow<ContactDeleteResult> = _deleteResult

    fun selectTab(tab: ContactGroupListTab) {
        _selectedTab.value = tab
    }

    fun reloadContactGroups() {
        loadingJob?.cancel()?.also {
            logger.debug("Cancelling loading-job to reload")
        }
        loadingJob = viewModelScope.launch {
            _contactGroups.withLoadingState {
                val contactGroups = contactGroupService.loadAllContactGroups(ContactType.SECRET)
                val numberOfContactsPerGroup = contactGroupService.loadNumberOfContactsPerGroup()
                contactGroups
                    .map { ContactGroupWithContactCount(it, numberOfContactsPerGroup[it.id.name] ?: 0) }
                    .sortedBy { it.contactGroup.id.name.lowercase() }
            }
        }
    }

    fun updateContactGroup(oldGroup: IContactGroup, newGroup: IContactGroup) {
        viewModelScope.launch {
            val result = _saveResult.withLoadingState {
                contactGroupService.updateContactGroup(oldGroup, newGroup)
            }

            if (result is ContactSaveResult.Success) {
                reloadContactGroups()
            }
        }
    }

    fun deleteContactGroup(contactGroup: IContactGroup) {
        viewModelScope.launch {
            val result = _deleteResult.withLoadingState {
                contactGroupService.deleteContactGroup(contactGroup)
            }

            if (result is ContactDeleteResult.Success) {
                reloadContactGroups()
            }
        }
    }

    fun resetSaveResult() {
        viewModelScope.launch {
            _saveResult.emitInactive()
        }
    }

    fun resetDeleteResult() {
        viewModelScope.launch {
            _deleteResult.emitInactive()
        }
    }
}
