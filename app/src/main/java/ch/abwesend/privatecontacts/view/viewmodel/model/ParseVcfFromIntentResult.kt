/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel.model

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.ParsedData

sealed interface ParseVcfFromIntentResult {
    data object Failure : ParseVcfFromIntentResult
    data class SingleContact(val contact: IContact) : ParseVcfFromIntentResult
    data class MultipleContacts(val parsedData: ParsedData) : ParseVcfFromIntentResult
}
