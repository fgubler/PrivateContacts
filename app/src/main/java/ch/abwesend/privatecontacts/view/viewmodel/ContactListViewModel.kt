package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.lib.flow.Debouncer
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val searchService: FullTextSearchService by injectAnywhere()

    @FlowPreview
    private val searchQueryDebouncer by lazy {
        Debouncer.debounce<String>(
            scope = viewModelScope,
            debounceMs = 300,
        ) { query ->
            _contacts.value = loadService.searchContacts(query)
        }
    }

    /**
     * The [State] is necessary to make sure the view is updated on [reloadContacts]
     */
    private val _contacts: MutableState<Flow<PagingData<IContactBase>>> by lazy {
        mutableStateOf(loadService.loadContacts())
    }
    val contacts: State<Flow<PagingData<IContactBase>>> = _contacts

    fun reloadContacts() {
        _contacts.value = loadService.loadContacts()
    }

    @FlowPreview
    fun changeSearchQuery(query: String) {
        val preparedQuery = searchService.prepareQuery(query)
        if (searchService.isLongEnough(preparedQuery)) {
            searchQueryDebouncer.newValue(preparedQuery)
        }
    }

    // TODO it is not very nice to expose the suspend function
    suspend fun resolveContact(contact: IContactBase) =
        loadService.resolveContact(contact)
}
