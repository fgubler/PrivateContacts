package ch.abwesend.privatecontacts.view.screens.importexport

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.ErrorDialog
import ch.abwesend.privatecontacts.view.components.text.SectionTitle
import ch.abwesend.privatecontacts.view.model.screencontext.IImportExportScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ImportExport
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.viewmodel.ImportExportViewModel
import java.io.File
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
object ImportExportScreen {
    private val VCF_MIME_TYPES = arrayOf("text/vcard", "text/x-vcard")

    @Composable
    fun Screen(screenContext: IImportExportScreenContext) {
        val viewModel = screenContext.importExportViewModel

        BaseScreen(screenContext = screenContext, selectedScreen = ImportExport) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                ImportCategory(viewModel = viewModel)
                ExportCategory(viewModel = viewModel)
            }
        }
    }

    @Composable
    private fun ImportCategory(viewModel: ImportExportViewModel) {
        var fileSelectionErrorPath: String? by remember { mutableStateOf(null) }

        Category(title = R.string.import_title) {
            VcfFilePicker(viewModel.importFile.value) { uri ->
                onFileSelected(viewModel, uri) { path ->
                    fileSelectionErrorPath = path
                }
            }

            fileSelectionErrorPath?.let {
                FileSelectionFailedDialog(filePath = it) { fileSelectionErrorPath = null }
            }
        }
    }

    @Composable
    private fun VcfFilePicker(selectedFile: File?, onFileSelected: (Uri?) -> Unit) {
        val rootDirectory = remember { Environment.getExternalStorageDirectory().absolutePath }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = onFileSelected,
        )

        val fieldValue = selectedFile?.absolutePath.orEmpty()
            .replace(oldValue = rootDirectory, newValue = "") // make the name shorter
            .trimStart(File.separatorChar)

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                label = { Text(text = stringResource(id = R.string.select_file_to_import)) },
                value = fieldValue,
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .clickable { editImportFilePath(launcher) },
                onValueChange = { newValue ->
                    logger.debug("Changed import file path to $newValue")
                }
            )
            EditIconButton { editImportFilePath(launcher) }
        }
    }

    private fun editImportFilePath(launcher: ManagedActivityResultLauncher<Array<String>, Uri?>) {
        launcher.launch(VCF_MIME_TYPES)
    }

    private fun onFileSelected(
        viewModel: ImportExportViewModel,
        uri: Uri?,
        onError: (path: String) -> Unit
    ) {
        logger.debugLocally("Selected uri '$uri'")
        val uriPath = uri?.path
        if (uri == null || uriPath == null) {
            logger.debug("No file selected") // user pressed "cancel"
            return
        }
        val selectedFile = viewModel.getSanitizedFileOrNull(uri)

        if (selectedFile == null) {
            onError(uriPath)
        } else {
            viewModel.setImportFile(selectedFile)
        }
    }

    @Composable
    private fun FileSelectionFailedDialog(filePath: String, onClose: () -> Unit) {
        logger.warning("Failed to get file from URI")
        val text = stringResource(id = R.string.failed_to_select_file, filePath)
        ErrorDialog(errorMessage = text, onClose = onClose)
    }

    @Composable
    private fun ExportCategory(viewModel: ImportExportViewModel) {
        Category(title = R.string.export_title) {
            Text(text = "Not yet implemented")
            // TODO implement
        }
    }

    @Composable
    private fun Category(@StringRes title: Int, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier
                .padding(all = 5.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SectionTitle(titleRes = title, addTopPadding = false)
                content()
            }
        }
    }
}
