/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.view.components.buttons.SecondaryButton
import ch.abwesend.privatecontacts.view.components.inputs.ContactTypeField
import ch.abwesend.privatecontacts.view.components.inputs.CreateFileFilePicker
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
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
            if (false) { // TODO implement logic and reinsert
                ExportCategoryContent(viewModel = viewModel)
            } else {
                Text(text = "Coming soon...", fontStyle = FontStyle.Italic)
            }
        }
    }

    @Composable
    private fun ExportCategoryContent(viewModel: ContactExportViewModel) {
        val filePath = viewModel.fileUri.value
        // TODO find a way to access the "future" name of the file to be created
        val dummyDisplayFilePath = filePath?.let { stringResource(id = R.string.export_file_selected) }.orEmpty()
        val sourceType = viewModel.sourceType.value

        ContactTypeField(
            labelRes = R.string.contact_type_to_export,
            selectedType = sourceType,
            showInfoButton = false,
            isScrolling = { parent.isScrolling },
        ) { newType ->
            viewModel.selectSourceType(newType)
            viewModel.selectFile(uri = null) // the file-name depends on the contact-type
        }

        Spacer(modifier = Modifier.height(10.dp))

        VcfFilePicker(
            displayFilePath = dummyDisplayFilePath,
            defaultFileName = createDefaultFileName(sourceType)
        ) { uri ->
            // in the case of "cancel", leave the old value
            uri?.let { viewModel.selectFile(it) }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SecondaryButton(
            enabled = filePath != null,
            onClick = { viewModel.exportContacts(sourceType) },
        ) {
            Text(
                text = stringResource(id = R.string.export_contacts),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun createDefaultFileName(sourceType: ContactType): String {
        val datePrefix = LocalDate.now().toString()
        val typePostfix = stringResource(id = sourceType.label)
        val extension = VCF_FILE_EXTENSION
        return "${datePrefix}_$typePostfix.$extension"
    }

    @Composable
    private fun VcfFilePicker(
        displayFilePath: String,
        defaultFileName: String,
        onFileSelected: (uri: Uri?) -> Unit
    ) {
        CreateFileFilePicker(
            labelRes = R.string.select_file_to_export_to,
            mimeType = VCF_MAIN_MIME_TYPE,
            displayFilePath = displayFilePath,
            defaultFileName = defaultFileName,
            onFileSelected = onFileSelected,
        )
    }
}
