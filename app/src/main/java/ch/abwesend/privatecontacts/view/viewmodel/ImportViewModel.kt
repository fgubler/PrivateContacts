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
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.service.ContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ImportViewModel : ViewModel() {
    private val importExportService: ContactImportExportService by injectAnywhere()

    private val _fileUri: MutableState<Uri?> = mutableStateOf(value = null)
    val fileUri: MutableState<Uri?> = _fileUri

    private val _targetType: MutableState<ContactType> = mutableStateOf(ContactType.SECRET)
    val targetType: State<ContactType> = _targetType

    private val _targetAccount: MutableState<ContactAccount?> = mutableStateOf(null)
    val targetAccount: State<ContactAccount?> = _targetAccount

    fun selectFile(uri: Uri?) {
        _fileUri.value = uri
    }

    fun selectTargetType(contactType: ContactType) {
        _targetType.value = contactType
    }

    fun selectTargetAccount(contactAccount: ContactAccount) {
        _targetAccount.value = contactAccount
    }

    fun importContacts(targetType: ContactType, targetAccount: ContactAccount) {
        val sourceFile = fileUri.value
        if (sourceFile == null) {
            logger.warning("Trying to import vcf file but no file is selected")
            return
        }
        logger.debugLocally("Importing vcf file '${sourceFile.path}' as $targetType in account ${targetAccount.type}")

        viewModelScope.launch {
            importExportService.importContacts(sourceFile, targetType, targetAccount)
        }
    }
}
