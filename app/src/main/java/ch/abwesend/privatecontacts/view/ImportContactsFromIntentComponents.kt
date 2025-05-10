/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData
import ch.abwesend.privatecontacts.view.components.dialogs.SaveCancelDialog
import ch.abwesend.privatecontacts.view.model.screencontext.IScreenContext
import ch.abwesend.privatecontacts.view.screens.importexport.ImportComponents.ProgressAndResultHandler
import ch.abwesend.privatecontacts.view.screens.importexport.ImportComponents.ReplaceExistingContactsCheckBox
import ch.abwesend.privatecontacts.view.screens.importexport.ImportComponents.TargetTypeFields
import ch.abwesend.privatecontacts.view.util.collectWithEffect
import ch.abwesend.privatecontacts.view.viewmodel.MainViewModel
import ch.abwesend.privatecontacts.view.viewmodel.model.ParseVcfFromIntentResult

@ExperimentalMaterialApi
object ImportContactsFromIntentComponents {
    @Composable
    fun ObserveVcfImportResult(
        viewModel: MainViewModel,
        screenContext: IScreenContext,
        onRevokeFilePermission: (Uri) -> Unit
    ) {
        val context = LocalContext.current

        var importMultipleDialogData: ContactImportPartialData.ParsedData?
            by remember { mutableStateOf(null) }

        viewModel.vcfParsingResult.collectWithEffect { result ->
            onRevokeFilePermission(result.fileUri) // the parsing is done => permission can be revoked
            when (result) {
                is ParseVcfFromIntentResult.Failure -> {
                    Toast.makeText(context, R.string.failed_to_import_contacts, Toast.LENGTH_SHORT).show()
                }
                is ParseVcfFromIntentResult.MultipleContacts -> {
                    importMultipleDialogData = result.parsedData
                }
                is ParseVcfFromIntentResult.SingleContact -> {
                    screenContext.navigateToContactEditScreen(result.contact)
                }
            }
        }

        importMultipleDialogData?.let { parsedData ->
            ImportMultipleContactsDialog(viewModel, screenContext, parsedData) { importMultipleDialogData = null }
        }
    }

    @Composable
    private fun ImportMultipleContactsDialog(
        viewModel: MainViewModel,
        screenContext: IScreenContext,
        parsedData: ContactImportPartialData.ParsedData,
        onCloseDialog: () -> Unit,
    ) {
        val settings = screenContext.settings
        var targetType by remember { mutableStateOf(settings.defaultContactType) }
        var selectedAccount by remember { mutableStateOf(settings.defaultExternalContactAccount) }
        var replaceExistingContacts by remember { mutableStateOf(false) }
        var importStarted by remember { mutableStateOf(false) }

        if (!importStarted) {
            SaveCancelDialog(
                title = R.string.import_contacts,
                content = {
                    Column {
                        TargetTypeFields(
                            targetType = targetType,
                            selectedAccount = selectedAccount,
                            onTargetTypeChanged = { targetType = it },
                            onTargetAccountChanged = { selectedAccount = it }
                        )

                        if (targetType == ContactType.SECRET) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ReplaceExistingContactsCheckBox(replaceExistingContacts) {
                                replaceExistingContacts = !replaceExistingContacts
                            }
                        }
                    }
                },
                onSave = {
                    viewModel.importContacts(parsedData, targetType, selectedAccount, replaceExistingContacts)
                    importStarted = true
                },
                onCancel = onCloseDialog,
            )
        }
        ProgressAndResultHandler(viewModel.contactImportResult) {
            viewModel.resetContactImportResult()
            screenContext.contactListViewModel.reloadContacts()
            importStarted = false
            onCloseDialog()
        }
    }
}
