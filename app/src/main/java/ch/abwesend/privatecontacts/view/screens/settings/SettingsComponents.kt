/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.components.inputs.DropDownComponent
import ch.abwesend.privatecontacts.view.components.text.SectionSubtitle
import ch.abwesend.privatecontacts.view.model.DropDownOption

@ExperimentalMaterialApi
private val parent = SettingsScreen

@Composable
fun SettingsCategory(
    @StringRes titleRes: Int,
    content: @Composable () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(10.dp)) {
            SectionSubtitle(titleRes = titleRes, addTopPadding = false)
            content()
        }
    }
}

@Composable
fun SettingsCheckbox(
    @StringRes label: Int,
    @StringRes description: Int?,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsLabel(labelRes = label)
            description?.let { SettingsDescription(descriptionRes = it) }
        }
        Checkbox(
            checked = value,
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
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier.heightIn(min = 50.dp).fillMaxWidth(),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                SettingsLabel(labelRes = label, modifier = Modifier.weight(1f))
                selectedOption?.let {
                    Text(it.getLabel(), modifier = Modifier.padding(start = 5.dp))
                }
            }
            description?.let { SettingsDescription(descriptionRes = it) }
        }
    }
}

@Composable
private fun SettingsLabel(@StringRes labelRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = labelRes),
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.body1,
        modifier = modifier,
    )
}

@Composable
private fun SettingsDescription(@StringRes descriptionRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = descriptionRes),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.body2,
        modifier = modifier,
    )
}
