/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.flow.EventFlow
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.emitInactive
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.withLoadingState
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportData
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardParseError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactImportService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.initialization.InitializationState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InitialInfoDialog
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.NOT_AUTHENTICATED
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.SUCCESS
import ch.abwesend.privatecontacts.view.viewmodel.model.ParseVcfFromIntentResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val dispatchers: IDispatchers by injectAnywhere()
    private val importService: ContactImportService by injectAnywhere()

    private val _initializationState: MutableState<InitializationState> = mutableStateOf(InitialInfoDialog)
    val initializationState: State<InitializationState> = _initializationState

    private val _authenticationStatus: MutableState<AuthenticationStatus> = mutableStateOf(NOT_AUTHENTICATED)
    val authenticationStatus: State<AuthenticationStatus> = _authenticationStatus

    private val _vcfParsingResult = EventFlow.createShared<ParseVcfFromIntentResult>()
    val vcfParsingResult: Flow<ParseVcfFromIntentResult> = _vcfParsingResult

    /**
     * Implemented as a resource to show a loading-indicator during import.
     * The [BinaryResult] would not be necessary in this case but lets us re-use the components from the Import-Screen
     */
    private val _contactImportResult = mutableResourceStateFlow<BinaryResult<ContactImportData, VCardParseError>>()
    val contactImportResult: ResourceFlow<BinaryResult<ContactImportData, VCardParseError>> = _contactImportResult

    fun goToNextState() {
        val oldState = _initializationState.value
        _initializationState.value = _initializationState.value.next()
        logger.debug(
            "Changed initializationState from ${oldState::class.java.simpleName} " +
                "to ${_initializationState.value::class.java.simpleName}"
        )
    }

    fun handleAuthenticationResult(authenticationFlow: Flow<AuthenticationStatus>) {
        viewModelScope.launch(dispatchers.default) {
            authenticationFlow.firstOrNull()?.let {
                _authenticationStatus.value = it
            }
        }
    }

    fun grantAccessWithoutAuthentication() {
        _authenticationStatus.value = SUCCESS
    }

    fun parseVcfFile(fileUri: Uri) {
        viewModelScope.launch {
            val loadResult = importService.loadContacts(fileUri, Settings.current.defaultContactType)
            loadResult.ifHasError {
                logger.warning("Failed to load contact(s) from VCF file: $it")
                _vcfParsingResult.emit(ParseVcfFromIntentResult.Failure)
            }.ifHasValue { data ->
                val contacts = data.successfulContacts
                logger.info(
                    "Parsed ${contacts.size} contact(s) from VCF file " +
                        "with ${data.numberOfFailedContacts} failures"
                )
                val result = if (contacts.size > 1) {
                    ParseVcfFromIntentResult.MultipleContacts(data)
                } else if (contacts.size == 1) {
                    ParseVcfFromIntentResult.SingleContact(contacts.first())
                } else {
                    ParseVcfFromIntentResult.Failure
                }
                _vcfParsingResult.emit(result)
            }
        }
    }

    fun importContacts(
        parsedContacts: ContactImportPartialData.ParsedData,
        targetType: ContactType,
        targetAccount: ContactAccount,
        replaceExisting: Boolean,
    ) {
        val contacts = parsedContacts.successfulContacts
        if (contacts.isEmpty()) {
            logger.warning("No contacts selected for import")
            return
        }
        logger.debug("Importing ${contacts.size} contacts as $targetType, ${if (replaceExisting) "" else "not "}replacing")

        viewModelScope.launch {
            _contactImportResult.withLoadingState {
                val result = importService.storeContacts(parsedContacts, targetType, targetAccount, replaceExisting)
                val numberOfChanged = result.newImportedContacts.size + result.replacedExistingContacts.size
                logger.debug("Import $numberOfChanged contact(s) successfully")
                SuccessResult(result)
            }
        }
    }

    fun resetContactImportResult() {
        viewModelScope.launch { _contactImportResult.emitInactive() }
    }
}
