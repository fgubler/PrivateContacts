/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

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
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.ContactTypeChangeService
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.model.ContactListScreenState
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.BulkMode
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Normal
import ch.abwesend.privatecontacts.view.model.ContactListScreenState.Search
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.ALL_CONTACTS
import ch.abwesend.privatecontacts.view.screens.contactlist.ContactListTab.SECRET_CONTACTS
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()
    private val typeChangeService: ContactTypeChangeService by injectAnywhere()

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

    private var bulkModeSelectedContacts: Set<ContactId> = emptySet()
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
    private val _deleteResult = mutableResourceStateFlow<ContactBatchChangeResult>()
    val deleteResult: ResourceFlow<ContactBatchChangeResult> = _deleteResult

    // TODO observe
    /** implemented as a resource to show a loading-indicator during type-change */
    private val _typeChangeResult = mutableResourceStateFlow<ContactBatchChangeResult>()
    val typeChangeResult: ResourceFlow<ContactBatchChangeResult> = _typeChangeResult

    /** to remember the scrolling-position after returning from an opened contact */
    val scrollingState: LazyListState = LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)

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
            _contacts.emitAll(contactsFlow)
        }
    }

    /** beware: do not cache the result (when returning from detail-screen we want to reload) */
    private suspend fun loadContacts(): ResourceFlow<List<IContactBase>> {
        logger.debug("Loading contacts")
        return when (selectedTab.value) {
            SECRET_CONTACTS -> loadService.loadSecretContacts()
            ALL_CONTACTS -> loadService.loadAllContacts()
        }
    }

    /** beware: do not cache the result (when returning from detail-screen we want to reload) */
    private suspend fun searchContacts(query: String): ResourceFlow<List<IContactBase>> {
        logger.debug("Searching contacts with query '$query'")
        return when (selectedTab.value) {
            SECRET_CONTACTS -> loadService.searchSecretContacts(query)
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
        bulkModeSelectedContacts = if (selectedContacts.contains(contactId)) {
            logger.debug("unselecting contact $contactId")
            selectedContacts.minus(contactId)
        } else {
            logger.debug("selecting contact $contactId")
            selectedContacts.plus(contactId)
        }
    }

    fun deleteContacts(contactIds: Set<ContactId>) {
        viewModelScope.launch {
            val result = _deleteResult.withLoadingState {
                saveService.deleteContacts(contactIds)
            }

            if (result == null || !result.completelyFailed) {
                launch { reloadContacts() }
                setBulkMode(enabled = false) // bulk-action is over
            }
        }
    }

    // TODO use
    fun changeContactType(contacts: List<IContact>, newType: ContactType) {
        viewModelScope.launch {
            val result = _deleteResult.withLoadingState {
                typeChangeService.changeContactType(contacts, newType)
            }

            if (result == null || !result.completelyFailed) {
                launch { reloadContacts() }
                setBulkMode(enabled = false) // bulk-action is over
            }
        }
    }

    fun resetDeletionResult() {
        viewModelScope.launch {
            _deleteResult.emitInactive()
        }
    }
}
