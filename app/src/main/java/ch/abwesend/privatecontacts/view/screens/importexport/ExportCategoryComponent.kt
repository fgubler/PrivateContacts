/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.filepicker.CreateFileFilePickerLauncher.Companion.rememberCreateFileLauncher
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ActionWithContactPermission.Companion.rememberActionWithContactPermission
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_FILE_EXTENSION
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MAIN_MIME_TYPE
import ch.abwesend.privatecontacts.view.viewmodel.ContactExportViewModel
import java.time.LocalDate
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ExportCategoryComponent {
    private val parent = ContactImportExportScreen // TODO remove once google issue 212091796 is fixed

    @Composable
    fun ExportCategory(viewModel: ContactExportViewModel) {
        ImportExportCategory(title = R.string.export_title) {
            ExportCategoryContent(viewModel = viewModel)
        }
    }

    @Composable
    private fun ExportCategoryContent(viewModel: ContactExportViewModel) {
        val selectedType = viewModel.sourceType.value

        ContactTypeField(
            labelRes = R.string.contact_type_to_export,
            selectedType = selectedType,
            showInfoButton = false,
            isScrolling = { parent.isScrolling },
        ) { newType -> viewModel.selectSourceType(newType) }

        Spacer(modifier = Modifier.height(10.dp))

        ExportButton(viewModel, selectedType)
    }

    @Composable
    private fun ExportButton(viewModel: ContactExportViewModel, sourceType: ContactType) {
        val defaultFileName = createDefaultFileName(sourceType = sourceType)
        val exportAction = rememberActionWithContactPermission()

        val launcher = rememberCreateFileLauncher(
            mimeType = VCF_MAIN_MIME_TYPE,
            defaultFilename = defaultFileName,
            onFileSelected = { targetFile -> viewModel.exportContacts(targetFile, sourceType) },
        )

        exportAction.VisibleComponent()

        SecondaryButton(
            onClick = {
                exportAction.executeAction(sourceType.androidPermissionRequired) { launcher.launch() }
            },
            content = {
                Text(
                    text = stringResource(id = R.string.export_contacts),
                    textAlign = TextAlign.Center
                )
            },
        )
    }

    @Composable
    private fun createDefaultFileName(sourceType: ContactType): String {
        val datePrefix = LocalDate.now().toString()
        val typePostfix = stringResource(id = sourceType.label)
        val extension = VCF_FILE_EXTENSION
        return "${datePrefix}_$typePostfix.$extension"
    }
}
