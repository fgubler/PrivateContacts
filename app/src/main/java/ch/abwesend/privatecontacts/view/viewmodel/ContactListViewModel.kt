/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.lib.flow.Debouncer
import ch.abwesend.privatecontacts.domain.lib.flow.EventFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactId
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()

    /** initially, the contacts are always returned to be empty before loading-state starts */
    var initialEmptyContactsIgnored: Boolean = false

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

    private var bulkModeSelectedContacts: Set<IContactId> = emptySet()
        set(value) {
            field = value
            updateScreenState()
        }

    private val _screenState: MutableState<ContactListScreenState> = mutableStateOf(Normal)
    val screenState: State<ContactListScreenState> = _screenState

    /** the currently applied search-filter for the list */
    private var currentFilter: String? = null

    @FlowPreview
    private val searchQueryDebouncer by lazy {
        Debouncer.debounce<String>(
            scope = viewModelScope,
            debounceMs = 300,
        ) { query ->
            currentFilter = query.ifEmpty { null }
            _contacts.value = searchContacts(query)
        }
    }

    private val _selectedTab = mutableStateOf(ContactListTab.default)
    val selectedTab: State<ContactListTab> = _selectedTab

    /**
     * The [State] is necessary to make sure the view is updated on [reloadContacts]
     */
    private val _contacts: MutableState<Flow<PagingData<IContactBase>>> by lazy {
        mutableStateOf(loadContacts())
    }
    val contacts: State<Flow<PagingData<IContactBase>>> = _contacts

    private val _deleteResult = EventFlow.createShared<ContactDeleteResult>()
    val deleteResult: Flow<ContactDeleteResult> = _deleteResult

    fun selectTab(tab: ContactListTab) {
        _selectedTab.value = tab
        reloadContacts()
    }

    fun reloadContacts(resetSearch: Boolean = false) {
        if (resetSearch) {
            resetSearch()
        }
        _contacts.value = currentFilter?.let { searchContacts(it) } ?: loadContacts()
    }

    /** beware: do not cache the result(when returning from detail-screen we want to reload) */
    private fun loadContacts(): Flow<PagingData<IContactBase>> {
        logger.debug("Loading contacts")
        initialEmptyContactsIgnored = false
        return when (selectedTab.value) {
            SECRET_CONTACTS -> loadService.loadSecretContacts()
            ALL_CONTACTS -> loadService.loadAllContacts()
        }
    }

    /** beware: do not cache the result(when returning from detail-screen we want to reload) */
    private fun searchContacts(query: String): Flow<PagingData<IContactBase>> {
        logger.debug("Searching contacts with query '$query'")
        initialEmptyContactsIgnored = false
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

    fun deleteContacts(contactIds: Set<IContactId>) {
        viewModelScope.launch {
            val result = saveService.deleteContacts(contactIds)
            _deleteResult.emit(result)

            if (result is ContactDeleteResult.Success) {
                launch { reloadContacts() }
                setBulkMode(enabled = false) // bulk-action is over
            }
        }
    }

    fun resetDeletionResult() {
        viewModelScope.launch {
            _deleteResult.emit(ContactDeleteResult.Inactive)
        }
    }
}
