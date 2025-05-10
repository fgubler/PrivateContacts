/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel.model

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData.ParsedData

sealed interface ParseVcfFromIntentResult {
    val fileUri: Uri

    data class Failure(override val fileUri: Uri) : ParseVcfFromIntentResult
    data class SingleContact(override val fileUri: Uri, val contact: IContact) : ParseVcfFromIntentResult
    data class MultipleContacts(override val fileUri: Uri, val parsedData: ParsedData) : ParseVcfFromIntentResult
}
