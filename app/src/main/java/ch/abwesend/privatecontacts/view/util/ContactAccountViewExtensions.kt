package ch.abwesend.privatecontacts.view.util

import ch.abwesend.privatecontacts.domain.model.contact.ContactType

val ContactType.accountSelectionRequired: Boolean
    get() = when (this) {
        ContactType.SECRET -> false
        ContactType.PUBLIC -> true
    }
