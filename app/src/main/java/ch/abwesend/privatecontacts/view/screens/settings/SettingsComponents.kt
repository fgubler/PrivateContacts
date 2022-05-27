/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.components.buttons.InfoIconButton
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.inputs.DropDownComponent
import ch.abwesend.privatecontacts.view.components.text.SectionSubtitle
import ch.abwesend.privatecontacts.view.model.DropDownOption
import ch.abwesend.privatecontacts.view.util.disabledContentColor
import ch.abwesend.privatecontacts.view.util.normalContentColor

@ExperimentalMaterialApi
private val parent = SettingsScreen

@Composable
fun SettingsCategorySpacer() = Spacer(modifier = Modifier.height(10.dp))

@Composable
fun SettingsEntryDivider() = Divider(modifier = Modifier.padding(vertical = 10.dp))

@Composable
fun SettingsCategory(
    @StringRes titleRes: Int,
    @StringRes infoPopupText: Int? = null,
    hideInfoPopup: Boolean = false,
    content: @Composable () -> Unit,
) {
    var showInfoPopup: Boolean by remember { mutableStateOf(false) }

    Card {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SectionSubtitle(titleRes = titleRes, addTopPadding = false)
                infoPopupText?.takeIf { !hideInfoPopup }?.let {
                    InfoIconButton { showInfoPopup = true }
                }
            }
            content()
        }
    }

    if (showInfoPopup && infoPopupText != null) {
        OkDialog(
            title = titleRes,
            text = infoPopupText,
        ) { showInfoPopup = false }
    }
}

@Composable
fun SettingsCheckbox(
    @StringRes label: Int,
    @StringRes description: Int?,
    value: Boolean,
    enabled: Boolean = true,
    onValueChanged: (Boolean) -> Unit,
) {
    val textColor = if (enabled) normalContentColor() else disabledContentColor()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onValueChanged(!value) },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsLabel(labelRes = label, textColor = textColor)
            description?.let { SettingsDescription(descriptionRes = it, textColor = textColor) }
        }
        Checkbox(
            checked = value,
            enabled = enabled,
            onCheckedChange = onValueChanged,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun <T> SettingsDropDown(
    @StringRes label: Int,
    @StringRes description: Int?,
    value: T,
    options: List<DropDownOption<T>>,
    onValueChanged: (T) -> Unit,
) {
    val selectedOption = remember(value) { options.find { it.value == value } }

    DropDownComponent(
        options = options,
        isScrolling = { parent.isScrolling },
        onValueChanged = onValueChanged,
    ) { _, modifier ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.heightIn(min = 45.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween,) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f),
                ) {
                    SettingsLabel(labelRes = label)
                    description?.let { SettingsDescription(descriptionRes = it) }
                }
                selectedOption?.let {
                    Text(it.getLabel(), modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsLabel(
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    textColor: Color = normalContentColor(),
) {
    Text(
        text = stringResource(id = labelRes),
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.body1,
        modifier = modifier,
        color = textColor,
    )
}

@Composable
private fun SettingsDescription(
    @StringRes descriptionRes: Int,
    modifier: Modifier = Modifier,
    textColor: Color = normalContentColor(),
) {
    Text(
        text = stringResource(id = descriptionRes),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.body2,
        color = textColor,
        modifier = modifier,
    )
}
