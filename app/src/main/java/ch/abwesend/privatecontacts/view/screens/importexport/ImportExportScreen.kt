package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.model.screencontext.IImportExportScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ImportExport
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.screens.importexport.ImportCategory.ImportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.viewmodel.ImportExportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
object ImportExportScreen {
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
    private fun ExportCategory(viewModel: ImportExportViewModel) {
        ImportExportCategory(title = R.string.export_title) {
            Text(text = "Not yet implemented")
            // TODO implement
        }
    }
}
