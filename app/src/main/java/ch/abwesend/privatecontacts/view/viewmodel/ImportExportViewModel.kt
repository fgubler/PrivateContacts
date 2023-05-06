/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult.FileReadingFailed
import ch.abwesend.privatecontacts.domain.model.result.ContactImportResult.VcfParsingFailed
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.FileReadService
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ImportExportViewModel : ViewModel() {
    private val importExportService: IContactImportExportService by injectAnywhere()
    private val fileReadService: FileReadService by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()

    private val _importFileUri: MutableState<Uri?> = mutableStateOf(value = null)
    val importFileUri: MutableState<Uri?> = _importFileUri

    fun setImportFile(uri: Uri?) {
        _importFileUri.value = uri
    }

    fun importContacts(targetType: ContactType) {
        val sourceFile = importFileUri.value
        if (sourceFile == null) {
            logger.warning("Trying to import file but no file is selected")
            return
        }

        logger.debugLocally("Importing vcf file '${sourceFile.path}' as $targetType")

        viewModelScope.launch {
            val fileContentResult = fileReadService.readFileContent(sourceFile)
            val contactsToImport = fileContentResult.mapValueSuspending { fileContent ->
                importExportService.loadContacts(fileContent, targetType)
            }
            val result = contactsToImport.mapValueSuspending { loadResult ->
                when (loadResult) {
                    is FileReadingFailed, is VcfParsingFailed -> loadResult
                    is ContactImportResult.Success -> {
                        // TODO create contacts
                    }
                }
            }
        }
    }
}
