/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.service.ContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ExportViewModel : ViewModel() {
    private val importExportService: ContactImportExportService by injectAnywhere()

    private val _fileUri: MutableState<Uri?> = mutableStateOf(value = null)
    val fileUri: MutableState<Uri?> = _fileUri

    private val _sourceType: MutableState<ContactType> = mutableStateOf(ContactType.SECRET)
    val sourceType: State<ContactType> = _sourceType

    fun selectFile(uri: Uri?) {
        _fileUri.value = uri
    }

    fun selectSourceType(contactType: ContactType) {
        _sourceType.value = contactType
    }

    fun exportContacts(sourceType: ContactType) {
        val sourceFile = fileUri.value
        if (sourceFile == null) {
            logger.warning("Trying to export to vcf file but no file is selected")
            return
        }
        logger.debugLocally("Exporting to vcf file from $sourceType")

        viewModelScope.launch {
            // TODO implement
        }
    }
}
