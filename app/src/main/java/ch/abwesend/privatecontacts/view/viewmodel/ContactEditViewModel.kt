/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.EventFlow
import ch.abwesend.privatecontacts.domain.lib.flow.mapReady
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditableWrapper
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactEditViewModel : ViewModel() {
    private val saveService: ContactSaveService by injectAnywhere()
    private val loadService: ContactLoadService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()

    var originalContact: IContact? = null
        private set

    var selectedContact: ContactEditableWrapper? by mutableStateOf(null)
        private set

    private val _saveResult = EventFlow.createShared<ContactSaveResult>()
    val saveResult: Flow<ContactSaveResult> = _saveResult

    private val _allContactGroups = mutableResourceStateFlow<List<IContactGroup>>()
    val allContactGroups = _allContactGroups.asStateFlow()

    val hasContactWritePermission: Boolean
        get() = permissionService.hasContactWritePermission()

    fun selectContact(contact: IContact) {
        val editable = contact.asEditable()
        originalContact = editable
        selectedContact = editable.deepCopy().wrap()
    }

    fun createContact() {
        val contact = ContactEditable.createNew()
        selectContact(contact)
    }

    fun changeContact(contact: IContactEditable) {
        selectedContact = contact.wrap()
    }

    fun saveContact(contact: IContactEditable) {
        viewModelScope.launch {
            val result = saveService.saveContact(contact)
            _saveResult.emit(result)
        }
    }

    fun clearContact() {
        selectedContact = null
    }

    fun loadAllContactGroups(contactType: ContactType) {
        viewModelScope.launch {
            _allContactGroups.withLoadingState { loadService.loadAllContactGroups(contactType) }
        }
    }

    /**
     * It is enough to add the group to the contact:
     * once the contact is saved, the new group will be persisted automatically.
     */
    fun createContactGroup(contactGroup: IContactGroup) {
        val newEmission = _allContactGroups.value
            .mapReady { groups -> (groups + contactGroup).sortedBy { it.id.name } }

        viewModelScope.launch {
            _allContactGroups.emit(newEmission)
        }
    }
}
