/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.view.components.buttons.InfoIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.model.ResDropDownOption

@ExperimentalMaterialApi
@Composable
fun ContactTypeField(
    selectedType: ContactType,
    isScrolling: () -> Boolean,
    @StringRes labelRes: Int = R.string.type,
    showInfoButton: Boolean = true,
    onValueChanged: (ContactType) -> Unit,
) {
    var showTypeInfoDialog: Boolean by remember { mutableStateOf(false) }

    val selectedOption = ResDropDownOption(labelRes = selectedType.label, value = selectedType)
    val options = ContactType.values().map {
        ResDropDownOption(labelRes = it.label, value = it)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.weight(1.0f)) {
            DropDownField(
                labelRes = labelRes,
                selectedOption = selectedOption,
                options = options,
                isScrolling = isScrolling,
                onValueChanged = onValueChanged
            )
        }

        if (showInfoButton) {
            InfoIconButton { showTypeInfoDialog = true }
        }
    }

    if (showTypeInfoDialog) {
        ContactTypeExplanationDialog { showTypeInfoDialog = false }
    }
}

@Composable
private fun ContactTypeExplanationDialog(onClose: () -> Unit) {
    OkDialog(
        title = R.string.visibility,
        onClose = onClose,
    ) {
        Column {
            ContactTypeExplanationChapter(
                titleRes = R.string.secret_contact,
                textRes = R.string.secret_contact_explanation,
            )
            Spacer(modifier = Modifier.height(10.dp))
            ContactTypeExplanationChapter(
                titleRes = R.string.public_contact,
                textRes = R.string.public_contact_explanation,
            )
        }
    }
}

@Composable
private fun ContactTypeExplanationChapter(@StringRes titleRes: Int, @StringRes textRes: Int) {
    Column {
        Text(text = stringResource(id = titleRes), fontWeight = FontWeight.Bold)
        Text(text = stringResource(id = textRes))
    }
}
