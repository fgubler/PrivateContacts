package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.model.screencontext.IImportExportScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ImportExport
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.screens.importexport.ImportCategoryComponent.ImportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.ImportExportScreenComponents.ImportExportCategory
import ch.abwesend.privatecontacts.view.viewmodel.ImportExportViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ImportExportScreen {
    var isScrolling: Boolean by mutableStateOf(false) // TODO remove once google issue 212091796 is fixed

    @Composable
    fun Screen(screenContext: IImportExportScreenContext) {
        val viewModel = screenContext.importExportViewModel
        val scrollState = rememberScrollState()
        isScrolling = scrollState.isScrollInProgress

        BaseScreen(screenContext = screenContext, selectedScreen = ImportExport) { padding ->
            Column(modifier = Modifier.padding(padding).verticalScroll(scrollState)) {
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
