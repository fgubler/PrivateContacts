/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import ch.abwesend.privatecontacts.view.model.ResDropDownOption
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.routing.Screen.Settings as SettingsScreen

@ExperimentalMaterialApi
object SettingsScreen {
    var isScrolling: Boolean by mutableStateOf(false) // TODO remove once google issue 212091796 is fixed
        private set

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val settingsRepository = Settings.current
        val currentSettings by settingsRepository.currentSettings.collectAsState(initial = SettingsState.defaultSettings)

        val scrollState = rememberScrollState()
        isScrolling = scrollState.isScrollInProgress

        BaseScreen(screenContext = screenContext, selectedScreen = SettingsScreen) {
            Column(modifier = Modifier.padding(10.dp).verticalScroll(scrollState)) {
                UxCategory(settingsRepository, currentSettings)
                Spacer(modifier = Modifier.height(10.dp))
                MiscellaneousCategory(settingsRepository, currentSettings)
            }
        }
    }

    @Composable
    private fun UxCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        val appThemeOptions = remember {
            AppTheme.values().map { ResDropDownOption(labelRes = it.labelRes, value = it) }
        }

        SettingsCategory(titleRes = R.string.settings_category_ux) {
            SettingsDropDown(
                label = R.string.settings_entry_app_theme,
                description = null,
                value = currentSettings.appTheme,
                options = appThemeOptions,
                onValueChanged = { settingsRepository.appTheme = it }
            )
            Divider()
            SettingsCheckbox(
                label = R.string.settings_entry_order_by_first_name,
                description = null,
                value = currentSettings.orderByFirstName,
                onValueChanged = { settingsRepository.orderByFirstName = it }
            )
        }
    }

    @Composable
    private fun MiscellaneousCategory(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        SettingsCategory(titleRes = R.string.settings_category_miscellaneous) {
            SettingsCheckbox(
                label = R.string.settings_entry_error_reports,
                description = R.string.settings_entry_error_reports_description,
                value = currentSettings.sendErrorsToCrashlytics,
                onValueChanged = { settingsRepository.sendErrorsToCrashlytics = it }
            )
        }
    }
}
