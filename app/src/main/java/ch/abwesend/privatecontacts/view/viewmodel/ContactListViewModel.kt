package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow

class ContactListViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()

    /**
     * The [State] is necessary to make sure the view is updated on [reloadContacts]
     */
    private val _contacts: MutableState<Flow<PagingData<ContactBase>>> by lazy {
        mutableStateOf(loadService.loadPagedContacts())
    }
    val contacts: State<Flow<PagingData<ContactBase>>> = _contacts

    fun reloadContacts() {
        _contacts.value = loadService.loadPagedContacts()
    }

    // TODO it is not very nice to expose the suspend function
    suspend fun resolveContact(contact: ContactBase) =
        loadService.resolveContact(contact)
}
