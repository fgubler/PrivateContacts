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
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.emitInactive
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.result.BinaryResult
import ch.abwesend.privatecontacts.domain.service.ContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ContactImportViewModel : ViewModel() {
    private val importExportService: ContactImportExportService by injectAnywhere()

    private val _fileUri: MutableState<Uri?> = mutableStateOf(value = null)
    val fileUri: MutableState<Uri?> = _fileUri

    private val _targetType: MutableState<ContactType> = mutableStateOf(ContactType.SECRET)
    val targetType: State<ContactType> = _targetType

    private val _targetAccount: MutableState<ContactAccount?> = mutableStateOf(null)
    val targetAccount: State<ContactAccount?> = _targetAccount

    /** implemented as a resource to show a loading-indicator during import */
    private val _importResult = mutableResourceStateFlow<BinaryResult<ContactImportData, VCardParseError>>()
    val importResult: ResourceFlow<BinaryResult<ContactImportData, VCardParseError>> = _importResult

    fun selectFile(uri: Uri?) {
        _fileUri.value = uri
    }

    fun selectTargetType(contactType: ContactType) {
        _targetType.value = contactType
    }

    fun selectTargetAccount(contactAccount: ContactAccount) {
        _targetAccount.value = contactAccount
    }

    fun resetImportResult() {
        logger.debug("Resetting contact import result")
        viewModelScope.launch {
            _importResult.emitInactive()
        }
    }

    fun importContacts(targetType: ContactType, targetAccount: ContactAccount) {
        val sourceFile = fileUri.value
        if (sourceFile == null) {
            logger.warning("Trying to import vcf file but no file is selected")
            return
        }
        logger.debugLocally("Importing vcf file '${sourceFile.path}' as $targetType in account ${targetAccount.type}")

        viewModelScope.launch {
            _importResult.withLoadingState {
                val result = importExportService.importContacts(sourceFile, targetType, targetAccount)
                logger.debug("Importing vcf file: result of type ${result.javaClass.simpleName}")
                result
            }
        }
    }
}
