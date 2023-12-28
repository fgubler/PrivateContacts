/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.flow.EventFlow
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.ContactTypeChangeService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactDetailViewModel : ViewModel() {
    private val loadService: ContactLoadService by injectAnywhere()
    private val saveService: ContactSaveService by injectAnywhere()
    private val typeChangeService: ContactTypeChangeService by injectAnywhere()
    private val exportService: ContactExportService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()

    private var latestSelectedContact: IContactBase? = null

    private val _selectedContact = mutableResourceStateFlow<IContact>()
    val selectedContact = _selectedContact.asStateFlow()

    private val _deleteResult = EventFlow.createShared<ContactDeleteResult>()
    val deleteResult: Flow<ContactDeleteResult> = _deleteResult

    private val _typeChangeResult = EventFlow.createShared<ContactSaveResult>()
    val typeChangeResult: Flow<ContactSaveResult> = _typeChangeResult

    private val _exportResult = EventFlow.createShared<BinaryResult<ContactExportData, VCardCreateError>>()
    val exportResult: Flow<BinaryResult<ContactExportData, VCardCreateError>> = _exportResult

    val hasContactWritePermission: Boolean
        get() = permissionService.hasContactWritePermission()

    fun selectContact(contact: IContactBase) {
        latestSelectedContact = contact
        viewModelScope.launch {
            _selectedContact.withLoadingState {
                loadService.resolveContact(contact.id)
            }
        }
    }

    fun reloadContact(contact: IContactBase? = latestSelectedContact) {
        contact?.let { selectContact(it) }
    }

    fun deleteContact(contact: IContactBase) {
        viewModelScope.launch {
            val result = saveService.deleteContact(contact)
            _deleteResult.emit(result)
        }
    }

    fun changeContactType(contact: IContact, newType: ContactType) {
        viewModelScope.launch {
            val result = typeChangeService.changeContactType(contact, newType)
            _typeChangeResult.emit(result)
        }
    }

    fun exportContacts(targetFile: Uri, vCardVersion: VCardVersion, contact: IContact) {
        logger.debugLocally("Exporting '${contact.displayName}' to vcf file.")

        viewModelScope.launch {
            val result = exportService.exportContacts(targetFile, vCardVersion, listOf(contact))
            logger.debug("Exported vcf file: result of type ${result.javaClass.simpleName}")
            _exportResult.emit(result)
        }
    }
}
