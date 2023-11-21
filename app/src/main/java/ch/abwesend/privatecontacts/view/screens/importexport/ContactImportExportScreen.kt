package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.abwesend.privatecontacts.view.model.screencontext.IContactImportExportScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.ImportExport
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.screens.importexport.ExportCategoryComponent.ExportCategory
import ch.abwesend.privatecontacts.view.screens.importexport.ImportCategoryComponent.ImportCategory
import kotlin.contracts.ExperimentalContracts

@ExperimentalMaterialApi
@ExperimentalContracts
object ContactImportExportScreen {
    @Composable
    fun Screen(screenContext: IContactImportExportScreenContext) {
        val scrollState = rememberScrollState()

        BaseScreen(screenContext = screenContext, selectedScreen = ImportExport) { padding ->
            Column(modifier = Modifier.padding(padding).verticalScroll(scrollState)) {
                ImportCategory(screenContext.importViewModel, screenContext.permissionProvider)
                ExportCategory(screenContext.exportViewModel, screenContext.permissionProvider)
            }
        }
    }
}
