package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.view.components.buttons.InfoIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.model.ResDropDownOption

@ExperimentalMaterialApi
@Composable
fun VCardVersionField(
    selectedVersion: VCardVersion,
    isScrolling: () -> Boolean,
    onValueChanged: (VCardVersion) -> Unit,
) {
    var showInfoDialog: Boolean by remember { mutableStateOf(false) }

    val selectedOption = ResDropDownOption(labelRes = selectedVersion.label, value = selectedVersion)
    val options = VCardVersion.values().map {
        ResDropDownOption(labelRes = it.label, value = it)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.weight(1.0f)) {
            DropDownField(
                labelRes = R.string.vcard_version_label,
                selectedOption = selectedOption,
                options = options,
                isScrolling = isScrolling,
                onValueChanged = onValueChanged
            )
        }

        InfoIconButton { showInfoDialog = true }
    }

    if (showInfoDialog) {
        VCardVersionInfoDialog { showInfoDialog = false }
    }
}

@Composable
private fun VCardVersionInfoDialog(onClose: () -> Unit) {
    OkDialog(
        title = R.string.vcard_versions_label,
        okButtonLabel = R.string.close,
        onClose = onClose,
        content = {
            Column {
                Text(text = stringResource(id = R.string.vcard_v3_label), fontWeight = FontWeight.Bold)
                Text(text = stringResource(id = R.string.vcard_v3_info_text))
                Text(text = stringResource(id = R.string.vcard_v3_info_text_relationships), modifier = Modifier.padding(start = 10.dp))
                Text(text = stringResource(id = R.string.vcard_v3_info_text_events), modifier = Modifier.padding(start = 10.dp))

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(id = R.string.vcard_v4_label), fontWeight = FontWeight.Bold)
                Text(text = stringResource(id = R.string.vcard_v4_info_text))
            }
        },
    )
}
