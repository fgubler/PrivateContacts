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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import ch.abwesend.privatecontacts.view.components.text.SectionSubtitle
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.routing.Screen.Settings as SettingsScreen

object SettingsScreen {
    @Composable
    fun Screen(screenContext: ScreenContext) {
        val settingsProvider = Settings.current
        val currentSettings by settingsProvider.currentSettings.collectAsState(initial = SettingsState.defaultSettings)

        BaseScreen(screenContext = screenContext, selectedScreen = SettingsScreen) {
            Column(modifier = Modifier.padding(10.dp)) {
                SettingsCategory(titleRes = R.string.settings_category_miscellaneous) {
                    SettingsCheckbox(
                        label = R.string.settings_entry_error_reports,
                        description = R.string.settings_entry_error_reports_description,
                        value = currentSettings.sendErrorsToCrashlytics,
                        onValueChanged = { settingsProvider.sendErrorsToCrashlytics = it }
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsCategory(
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
    private fun SettingsCheckbox(
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
                Text(
                    text = stringResource(id = label),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.body1,
                )
                description?.let {
                    Text(
                        text = stringResource(id = it),
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.body2,
                    )
                }
            }
            Checkbox(
                checked = value,
                onCheckedChange = onValueChanged,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}
