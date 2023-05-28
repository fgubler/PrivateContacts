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
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.service.ContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ImportExportViewModel : ViewModel() {
    private val importExportService: ContactImportExportService by injectAnywhere()

    private val _importFileUri: MutableState<Uri?> = mutableStateOf(value = null)
    val importFileUri: MutableState<Uri?> = _importFileUri

    private val _exportFileUri: MutableState<Uri?> = mutableStateOf(value = null)
    val exportFileUri: MutableState<Uri?> = _exportFileUri

    fun setImportFile(uri: Uri?) {
        _importFileUri.value = uri
    }

    fun setExportFile(uri: Uri?) {
        _exportFileUri.value = uri
    }

    fun importContacts(targetType: ContactType, targetAccount: ContactAccount) {
        val sourceFile = importFileUri.value
        if (sourceFile == null) {
            logger.warning("Trying to import vcf file but no file is selected")
            return
        }
        logger.debugLocally("Importing vcf file '${sourceFile.path}' as $targetType in account ${targetAccount.type}")

        viewModelScope.launch {
            importExportService.importContacts(sourceFile, targetType, targetAccount)
        }
    }

    fun exportContacts(sourceType: ContactType) {
        val sourceFile = exportFileUri.value
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
