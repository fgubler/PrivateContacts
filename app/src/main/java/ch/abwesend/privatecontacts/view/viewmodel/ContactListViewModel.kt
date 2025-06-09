/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.Debouncer
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.MutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.emitInactive
import ch.abwesend.privatecontacts.domain.lib.flow.mapReady
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactBaseWithAccountInformation
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.ContactTypeChangeService
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.model.ContactListScreenState
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.BulkMode
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Normal
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Search
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.ALL_CONTACTS
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.PUBLIC_CONTACTS
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.SECRET_CONTACTS
import ch.abwesend.privatecontacts.view.viewmodel.model.BulkContactDeleteResult
import ch.abwesend.privatecontacts.view.viewmodel.model.BulkContactExportResult
import ch.abwesend.privatecontacts.view.viewmodel.model.BulkOperationResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()
    private val typeChangeService: ContactTypeChangeService by injectAnywhere()
    private val exportService: ContactExportService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()

    private var showSearch: Boolean = false
        set(value) {
            field = value
            updateScreenState()
        }

    /** current content of the search-field (might not have started filtering for it yet) */
    private var searchText: String = ""
        set(value) {
            field = value
            updateScreenState()
        }

    private var bulkModeEnabled: Boolean = false
        set(value) {
            field = value
            updateScreenState()
        }

    private var bulkModeSelectedContacts: Set<IContactBase> = emptySet()
        set(value) {
            field = value
            updateScreenState()
        }

    private val _screenState: MutableState<ContactListScreenState> = mutableStateOf(Normal)
    val screenState: State<ContactListScreenState> = _screenState

    /** the currently applied search-filter for the list */
    private var currentFilter: String? = null
    private var contactLoadingJob: Job? = null

    @FlowPreview
    private val searchQueryDebouncer by lazy {
        Debouncer.debounce<String>(
            scope = viewModelScope,
            debounceMs = 300,
        ) { query ->
            currentFilter = query.ifEmpty { null }
            reloadContacts()
        }
    }

    private val _selectedTab = mutableStateOf(ContactListTab.default)
    val selectedTab: State<ContactListTab> = _selectedTab

    /**
     * The [State] is necessary to make sure the view is updated on [reloadContacts]
     */
    private val _contacts: MutableResourceStateFlow<List<IContactBase>> =
        mutableResourceStateFlow(InactiveResource())
    val contacts: ResourceStateFlow<List<IContactBase>> = _contacts

    /** implemented as a resource to show a loading-indicator during deletion */
    private val _deleteResult = mutableResourceStateFlow<BulkContactDeleteResult>()
    val deleteResult: ResourceFlow<BulkContactDeleteResult> = _deleteResult

    /** implemented as a resource to show a loading-indicator during type-change */
    private val _typeChangeResult = mutableResourceStateFlow<ContactIdBatchChangeResult>()
    val typeChangeResult: ResourceFlow<ContactIdBatchChangeResult> = _typeChangeResult

    /** implemented as a resource to show a loading-indicator during export */
    private val _exportResult = mutableResourceStateFlow<BulkContactExportResult>()
    val exportResult: ResourceFlow<BulkContactExportResult> = _exportResult

    /** to remember the scrolling-position after returning from an opened contact */
    val scrollingState: LazyListState = LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)

    val hasContactWritePermission: Boolean
        get() = permissionService.hasContactWritePermission()

    fun selectTab(tab: ContactListTab) {
        _selectedTab.value = tab
        reloadContacts()
    }

    fun reloadContacts(resetSearch: Boolean = false) {
        if (resetSearch) {
            resetSearch()
        }
        contactLoadingJob?.cancel()?.also {
            logger.debug("Cancelling loading-job to reload")
        }
        contactLoadingJob = viewModelScope.launch {
            val contactsFlow = currentFilter?.let { searchContacts(it) } ?: loadContacts()
            val sortedContactsFlow = contactsFlow.mapReady { contacts -> contacts.sortedBy { it.displayName } }
            _contacts.emitAll(sortedContactsFlow)
        }
    }

    /** beware: do not cache the result (when returning from detail-screen we want to reload) */
    private suspend fun loadContacts(): ResourceFlow<List<IContactBase>> {
        logger.debug("Loading contacts")
        return when (selectedTab.value) {
            SECRET_CONTACTS -> loadService.loadSecretContacts()
            PUBLIC_CONTACTS -> loadService.loadAndroidContacts()
            ALL_CONTACTS -> loadService.loadAllContacts()
        }
    }

    /** beware: do not cache the result (when returning from detail-screen we want to reload) */
    private suspend fun searchContacts(query: String): ResourceFlow<List<IContactBase>> {
        logger.debug("Searching contacts with query '$query'")
        return when (selectedTab.value) {
            SECRET_CONTACTS -> loadService.searchSecretContacts(query)
            PUBLIC_CONTACTS -> loadService.searchAndroidContacts(query)
            ALL_CONTACTS -> loadService.searchAllContacts(query)
        }
    }

    private fun resetSearch() {
        showSearch = false
        searchText = ""
        currentFilter = null
    }

    fun showSearch() {
        showSearch = true
    }

    fun setBulkMode(enabled: Boolean) {
        bulkModeEnabled = enabled
        if (!enabled) {
            bulkModeSelectedContacts = emptySet()
        }
    }

    @FlowPreview
    fun changeSearchQuery(query: String) {
        searchText = query
        val preparedQuery = searchService.prepareQuery(query)
        if (preparedQuery.isEmpty() || searchService.isLongEnough(preparedQuery)) {
            searchQueryDebouncer.newValue(preparedQuery)
        }
    }

    private fun updateScreenState() {
        val newState: ContactListScreenState = when {
            bulkModeEnabled -> BulkMode(selectedContacts = bulkModeSelectedContacts)
            showSearch -> Search(searchText = searchText)
            else -> Normal
        }
        _screenState.value = newState
    }

    fun toggleContactSelected(contact: IContactBase) {
        if (!bulkModeEnabled) {
            logger.warning("Tried to toggle contact-selection but bulk-mode is disabled.")
            return
        }

        val contactId = contact.id
        val selectedContacts = bulkModeSelectedContacts
        bulkModeSelectedContacts = if (selectedContacts.any { it.id == contactId }) {
            logger.debug("unselecting contact $contactId")
            selectedContacts.minus(contact)
        } else {
            logger.debug("selecting contact $contactId")
            selectedContacts.plus(contact)
        }
    }

    fun selectAllContacts() {
        _contacts.value.valueOrNull?.let { allContacts ->
            bulkModeSelectedContacts = allContacts.toSet()
        }
    }

    fun deselectAllContacts() {
        bulkModeSelectedContacts = emptySet()
    }

    fun deleteContacts(contactIds: Set<ContactId>) {
        viewModelScope.launch {
            val bulkOperationResult = _deleteResult.withLoadingState {
                BulkOperationResult(saveService.deleteContacts(contactIds), contactIds.size)
            }

            if (bulkOperationResult == null || !bulkOperationResult.result.completelyFailed) {
                launch { reloadContacts() }
                setBulkMode(enabled = false) // bulk-action is over
            }
        }
    }

    fun changeContactType(contacts: Collection<IContactBaseWithAccountInformation>, newType: ContactType) {
        viewModelScope.launch {
            val result = _typeChangeResult.withLoadingState {
                typeChangeService.changeContactType(contacts, newType)
            }

            if (result == null || !result.completelyFailed) {
                launch { reloadContacts() }
                setBulkMode(enabled = false) // bulk-action is over
            }
        }
    }

    fun exportContacts(targetFile: Uri, vCardVersion: VCardVersion, baseContacts: Collection<IContactBase>) {
        logger.debug("Exporting ${baseContacts.size} to vcf file.")

        viewModelScope.launch {
            val contactIds = baseContacts.map { it.id }
            val contacts = loadService.resolveContacts(contactIds)
            _exportResult.withLoadingState {
                val result = exportService.exportContacts(targetFile, vCardVersion, contacts)
                logger.debug("Exported vcf file: result of type ${result.javaClass.simpleName}")
                BulkOperationResult(result, baseContacts.size)
            }
            setBulkMode(enabled = false) // bulk-action is over
        }
    }

    fun resetDeletionResult() {
        viewModelScope.launch {
            _deleteResult.emitInactive()
        }
    }

    fun resetTypeChangeResult() {
        viewModelScope.launch {
            _typeChangeResult.emitInactive()
        }
    }

    fun resetExportResult() {
        viewModelScope.launch {
            _exportResult.emitInactive()
        }
    }
}
