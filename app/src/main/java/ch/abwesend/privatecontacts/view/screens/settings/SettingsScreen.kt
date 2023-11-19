/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.callIdentificationPossible
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField
import ch.abwesend.privatecontacts.view.components.inputs.VCardVersionField
import ch.abwesend.privatecontacts.view.initialization.CallPermissionHandler
import ch.abwesend.privatecontacts.view.model.ResDropDownOption
import ch.abwesend.privatecontacts.view.model.screencontext.ISettingsScreenContext
import ch.abwesend.privatecontacts.view.permission.IPermissionProvider
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.screens.settings.SettingsComponents.SettingsCategory
import ch.abwesend.privatecontacts.view.screens.settings.SettingsComponents.SettingsCategorySpacer
import ch.abwesend.privatecontacts.view.screens.settings.SettingsComponents.SettingsCheckbox
import ch.abwesend.privatecontacts.view.screens.settings.SettingsComponents.SettingsDropDown
import ch.abwesend.privatecontacts.view.screens.settings.SettingsComponents.SettingsEntryDivider
import ch.abwesend.privatecontacts.view.util.getCurrentActivity
import kotlin.contracts.ExperimentalContracts
import ch.abwesend.privatecontacts.view.routing.Screen.Settings as SettingsScreen

@ExperimentalMaterialApi
@ExperimentalContracts
object SettingsScreen {
    @Composable
    fun Screen(screenContext: ISettingsScreenContext) {
        val settingsRepository = Settings.repository
        val currentSettings = screenContext.settings
        val permissionProvider = screenContext.permissionProvider

        val scrollState = rememberScrollState()

        val callDetectionPossible = remember { callIdentificationPossible }

        BaseScreen(
            screenContext = screenContext,
            selectedScreen = SettingsScreen,
            topBarActions = { SettingsActions(screenContext.settingsViewModel) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                UxCategory(settingsRepository, currentSettings)
                SettingsCategorySpacer()

                if (callDetectionPossible) CallDetectionCategory(permissionProvider, settingsRepository, currentSettings)
                else CallDetectionCategoryDummy()
                SettingsCategorySpacer()

                AndroidContactsCategory(permissionProvider, settingsRepository, currentSettings)
                SettingsCategorySpacer()

                DefaultValuesCategory(permissionProvider, settingsRepository, currentSettings)
                SettingsCategorySpacer()

                MiscellaneousCategory(settingsRepository, currentSettings)
                SettingsCategorySpacer() // makes sure the last card is not cut off
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

            SettingsEntryDivider()

            SettingsCheckbox(
                label = R.string.settings_entry_order_by_first_name,
                description = null,
                value = currentSettings.orderByFirstName,
                onValueChanged = { settingsRepository.orderByFirstName = it }
            )
            SettingsCheckbox(
                label = R.string.settings_entry_show_contact_types_on_contact_list,
                description = null,
                value = currentSettings.showContactTypeInList,
                onValueChanged = { settingsRepository.showContactTypeInList = it }
            )

            SettingsEntryDivider()

            SettingsCheckbox(
                label = R.string.settings_entry_show_extra_buttons_in_edit_screen,
                description = null,
                value = currentSettings.showExtraButtonsInEditScreen,
                onValueChanged = { settingsRepository.showExtraButtonsInEditScreen = it }
            )
            SettingsCheckbox(
                label = R.string.settings_entry_invert_top_and_bottom_bars,
                description = null,
                value = currentSettings.invertTopAndBottomBars,
                onValueChanged = {
                    settingsRepository.invertTopAndBottomBars = it
                    settingsRepository.showExtraButtonsInEditScreen = !it // the buttons don't make sense if true
                }
            )
        }
    }

    @Composable
    private fun CallDetectionCategory(permissionProvider: IPermissionProvider, settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        var requestPermissions: Boolean by remember { mutableStateOf(false) }

        SettingsCategory(titleRes = R.string.settings_category_call_detection, infoPopupText = R.string.settings_info_dialog_call_detection) {
            SettingsCheckbox(
                label = R.string.settings_entry_call_detection,
                description = R.string.settings_entry_call_detection_description,
                value = currentSettings.observeIncomingCalls,
            ) {
                settingsRepository.observeIncomingCalls = it
                settingsRepository.requestIncomingCallPermissions = true
                if (it) { requestPermissions = true }
            }

            SettingsEntryDivider()

            SettingsCheckbox(
                label = R.string.settings_entry_show_calls_on_lockscreen,
                description = R.string.settings_entry_show_calls_on_lockscreen_description,
                value = currentSettings.observeIncomingCalls && currentSettings.showIncomingCallsOnLockScreen,
                enabled = currentSettings.observeIncomingCalls
            ) { settingsRepository.showIncomingCallsOnLockScreen = it }
        }

        if (requestPermissions) {
            getCurrentActivity()?.CallPermissionHandler(
                settings = currentSettings,
                permissionHelper = permissionProvider.callPermissionHelper,
                roleHelper = permissionProvider.callScreeningRoleHelper,
            ) {
                requestPermissions = false
            } ?: logger.warning("Activity not found: cannot ask for permissions")
        }
    }

    @Composable
    private fun CallDetectionCategoryDummy() {
        SettingsCategory(titleRes = R.string.settings_category_call_detection) {
            Text(text = stringResource(id = R.string.settings_category_call_detection_excuse))
        }
    }

    @Composable
    private fun AndroidContactsCategory(
        permissionProvider: IPermissionProvider,
        settingsRepository: SettingsRepository,
        currentSettings: ISettingsState,
    ) {
        SettingsCategory(
            titleRes = R.string.settings_category_contacts,
            infoPopupText = R.string.settings_info_dialog_android_contacts_permission,
            hideInfoPopup = currentSettings.showAndroidContacts,
        ) {
            SettingsCheckbox(
                label = R.string.settings_entry_show_android_contacts,
                description = R.string.settings_entry_show_android_contacts_description,
                value = currentSettings.showAndroidContacts,
            ) { newValue -> onShowAndroidContactsChanged(permissionProvider, settingsRepository, newValue) }
        }
    }

    private fun onShowAndroidContactsChanged(
        permissionProvider: IPermissionProvider,
        settingsRepository: SettingsRepository,
        newValue: Boolean,
    ) {
        if (newValue) {
            permissionProvider.contactPermissionHelper.requestAndroidContactPermissions { result ->
                logger.debug("Android contact permissions: $result")
                settingsRepository.showAndroidContacts = result.usable
            }
        } else {
            settingsRepository.showAndroidContacts = false
        }
    }

    @Composable
    private fun DefaultValuesCategory(
        permissionProvider: IPermissionProvider,
        settingsRepository: SettingsRepository,
        currentSettings: ISettingsState
    ) {
        SettingsCategory(titleRes = R.string.settings_category_default_values) {
            DefaultContactTypeField(permissionProvider, settingsRepository, currentSettings)
            Divider()
            DefaultContactAccountField(settingsRepository, currentSettings)
            Divider()
            DefaultVCardVersionField(settingsRepository, currentSettings)
        }
    }

    @Composable
    private fun DefaultContactTypeField(
        permissionProvider: IPermissionProvider,
        settingsRepository: SettingsRepository,
        currentSettings: ISettingsState,
    ) {
        val contactTypeOptions = remember {
            ContactType.values().map { ResDropDownOption(labelRes = it.label, value = it) }
        }
        var requestPermissionsFor: ContactType? by remember { mutableStateOf(null) }

        SettingsDropDown(
            label = R.string.settings_entry_default_contact_type,
            description = R.string.settings_entry_default_contact_type_description,
            value = currentSettings.defaultContactType,
            options = contactTypeOptions,
            onValueChanged = {
                if (!it.androidPermissionRequired) {
                    settingsRepository.defaultContactType = it
                } else {
                    requestPermissionsFor = it
                }
            }
        )

        val targetType = requestPermissionsFor
        if (targetType != null) {
            permissionProvider.contactPermissionHelper.requestAndroidContactPermissions {
                requestPermissionsFor = null
                if (it.usable) {
                    settingsRepository.defaultContactType = targetType
                }
            }
        }
    }

    @Composable
    private fun DefaultContactAccountField(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        AccountSelectionDropDownField(
            selectedAccount = currentSettings.defaultExternalContactAccount,
            onValueChanged = { settingsRepository.defaultExternalContactAccount = it }
        ) { options, selectedOption, onOptionSelected ->
            SettingsDropDown(
                label = R.string.settings_entry_default_external_contact_account,
                description = R.string.settings_entry_default_external_contact_account_description,
                value = selectedOption.value,
                options = options,
                onValueChanged = onOptionSelected
            )
        }
    }

    @Composable
    private fun DefaultVCardVersionField(settingsRepository: SettingsRepository, currentSettings: ISettingsState) {
        VCardVersionField(
            selectedVersion = currentSettings.defaultVCardVersion,
            onValueChanged = { settingsRepository.defaultVCardVersion = it },
        ) { options, selectedOption, onOptionSelected ->
            SettingsDropDown(
                label = R.string.settings_entry_default_vcard_version,
                description = R.string.settings_entry_default_vcard_version_description,
                value = selectedOption.value,
                options = options,
                onValueChanged = onOptionSelected
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
