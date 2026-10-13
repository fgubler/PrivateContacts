package ch.abwesend.privatecontacts.view.screens.contactedit

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
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.asEditable
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.service.ContactGroupService
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.routing.ContactEditRoute
import ch.abwesend.privatecontacts.view.routing.ContactEditTarget
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactEditViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val groupService: ContactGroupService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()

    private var initializedInstanceId: Int? = null
    private var loadingJob: Job? = null

    var originalContact: IContact? = null
        private set

    var selectedContact: ContactEditableWrapper? by mutableStateOf(null)
        private set

    /** only relevant for [ContactEditTarget.Existing]: the other targets do not need to load anything */
    private val _contactLoadingState = mutableResourceStateFlow<IContact>()
    val contactLoadingState = _contactLoadingState.asStateFlow()

    private val _saveResult = EventFlow.createShared<ContactSaveResult>()
    val saveResult: Flow<ContactSaveResult> = _saveResult

    private val _allContactGroups = mutableResourceStateFlow<List<IContactGroup>>()
    val allContactGroups = _allContactGroups.asStateFlow()

    val hasContactWritePermission: Boolean
        get() = permissionService.hasContactWritePermission()

    /**
     * Only initializes the contact once per [ContactEditRoute.instanceId]:
     * e.g. a rotation must not discard the changes the user already made.
     */
    fun initialize(route: ContactEditRoute) {
        if (route.instanceId != initializedInstanceId) {
            initializedInstanceId = route.instanceId
            loadingJob?.cancel()
            when (val target = route.target) {
                is ContactEditTarget.New -> selectContact(ContactEditable.createNew())
                is ContactEditTarget.Unsaved -> selectContact(target.contact)
                is ContactEditTarget.Existing -> loadContact(target.contactId)
            }
        }
    }

    private fun loadContact(contactId: ContactId) {
        originalContact = null
        selectedContact = null
        loadingJob = viewModelScope.launch {
            val contact = _contactLoadingState.withLoadingState { loadService.resolveContact(contactId) }
            contact?.let { selectContact(it) }
        }
    }

    private fun selectContact(contact: IContact) {
        val editable = contact.asEditable()
        originalContact = editable
        selectedContact = editable.deepCopy().wrap()
    }

    fun changeContact(contact: IContactEditable) {
        selectedContact = contact.wrap()
    }

    fun saveContact(contact: IContactEditable) {
        viewModelScope.launch {
            val result = saveService.saveContact(contact)

            when (result) {
                is ContactSaveResult.Success -> {
                    // make sure that it is not saved as "new" a second time
                    selectedContact = contact.deepCopy(isContactNew = false).wrap()
                }
                is ContactSaveResult.Failure,
                is ContactSaveResult.ValidationFailure -> Unit
            }
            _saveResult.emit(result)
        }
    }

    fun clearContact() {
        selectedContact = null
    }

    fun loadAllContactGroups(contactType: ContactType) {
        viewModelScope.launch {
            _allContactGroups.withLoadingState { groupService.loadAllContactGroups(contactType) }
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
