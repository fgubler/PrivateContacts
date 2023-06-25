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
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ContactExportViewModel : ViewModel() {
    private val exportService: ContactExportService by injectAnywhere()

    private val _sourceType: MutableState<ContactType> = mutableStateOf(Settings.current.defaultContactType)
    val sourceType: State<ContactType> = _sourceType

    // TODO extract to common logic
    /** implemented as a resource to show a loading-indicator during export */
    private val _exportResult = mutableResourceStateFlow<BinaryResult<ContactExportData, VCardCreateError>>()
    val exportResult: ResourceFlow<BinaryResult<ContactExportData, VCardCreateError>> = _exportResult

    fun selectSourceType(contactType: ContactType) {
        _sourceType.value = contactType
    }

    fun resetExportResult() {
        logger.debug("Resetting contact export result")
        viewModelScope.launch {
            _exportResult.emitInactive()
        }
    }

    fun exportContacts(targetFile: Uri?, sourceType: ContactType) {
        if (targetFile == null) {
            logger.warning("Trying to export to vcf file but no file is selected")
            return
        }
        logger.debugLocally("Exporting to vcf file from $sourceType")

        viewModelScope.launch {
            _exportResult.withLoadingState {
                val result = exportService.exportContacts(targetFile, sourceType)
                logger.debug("Exported vcf file: result of type ${result.javaClass.simpleName}")
                result
            }
        }
    }
}
