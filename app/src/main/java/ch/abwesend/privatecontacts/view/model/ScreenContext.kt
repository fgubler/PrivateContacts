package ch.abwesend.privatecontacts.view.model

import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel

data class ScreenContext(
    val router: AppRouter,
    val contactListViewModel: ContactListViewModel,
)
