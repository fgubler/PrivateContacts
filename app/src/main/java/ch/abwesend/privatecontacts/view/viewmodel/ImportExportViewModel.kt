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
import ch.abwesend.privatecontacts.domain.service.FilePickerSanitizingService
import ch.abwesend.privatecontacts.domain.service.interfaces.IContactImportExportService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch
import java.io.File

class ImportExportViewModel : ViewModel() {
    private val sanitizingService: FilePickerSanitizingService by injectAnywhere()
    private val importExportService: IContactImportExportService by injectAnywhere()

    private val _importFile: MutableState<File?> = mutableStateOf(null)
    val importFile: State<File?> = _importFile

    fun getSanitizedFileOrNull(uri: Uri): File? =
        uri.path?.let { sanitizingService.getValidFileOrNull(it) }

    fun setImportFile(path: File) {
        _importFile.value = path
    }

    fun importContacts(targetType: ContactType) {
        val sourceFile = importFile.value
        if (sourceFile == null) {
            logger.warning("Trying to import file but no file is selected")
            return
        }

        logger.debugLocally("Importing vcf file '${sourceFile.absolutePath}' as $targetType")

        viewModelScope.launch {
            val result = importExportService.importContacts(sourceFile, targetType)
            logger.debug("Import complete: $result")
            // TODO implement
        }
    }
}
