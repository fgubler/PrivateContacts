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
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()

    /** initially, the contacts are always returned to be empty before loading-state starts */
    var initialEmptyContactsIgnored: Boolean = false

    val showSearch: MutableState<Boolean> = mutableStateOf(false)

    /** current content of the search-field (might not have started filtering for it yet) */
    private val _searchText: MutableState<String> = mutableStateOf("")
    val searchText: State<String> = _searchText

    /** the currently applied search-filter for the list */
    private var currentFilter: String? = null

    @FlowPreview
    private val searchQueryDebouncer by lazy {
        Debouncer.debounce<String>(
            scope = viewModelScope,
            debounceMs = 300,
        ) { query ->
            currentFilter = query
            _contacts.value = searchContacts(query)
        }
    }

    /**
     * The [State] is necessary to make sure the view is updated on [reloadContacts]
     */
    private val _contacts: MutableState<Flow<PagingData<IContactBase>>> by lazy {
        mutableStateOf(loadContacts())
    }
    val contacts: State<Flow<PagingData<IContactBase>>> = _contacts

    fun reloadContacts(resetSearch: Boolean = false) {
        if (resetSearch) {
            resetSearch()
        }
        _contacts.value = currentFilter?.let { searchContacts(it) } ?: loadContacts()
    }

    private fun loadContacts(): Flow<PagingData<IContactBase>> {
        logger.debug("Loading contacts")
        initialEmptyContactsIgnored = false
        return loadService.loadContacts() // do not cache (when returning from detail-screen we want to reload)
    }

    private fun searchContacts(query: String): Flow<PagingData<IContactBase>> {
        logger.debug("Searching contacts with query '$query'")
        initialEmptyContactsIgnored = false
        return loadService.searchContacts(query)
    }

    private fun resetSearch() {
        showSearch.value = false
        _searchText.value = ""
        currentFilter = null
    }

    @FlowPreview
    fun changeSearchQuery(query: String) {
        _searchText.value = query
        val preparedQuery = searchService.prepareQuery(query)
        if (searchService.isLongEnough(preparedQuery)) {
            searchQueryDebouncer.newValue(preparedQuery)
        }
    }
}
