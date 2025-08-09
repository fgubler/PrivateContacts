/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.service.interfaces.AccountService
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.view.components.inputs.helper.DropDownFieldProvider
import ch.abwesend.privatecontacts.view.model.DropDownOption
import ch.abwesend.privatecontacts.view.model.DynamicStringDropDownOption

/** Dropdown to select from the available accounts on the device into which contacts can be saved */
@ExperimentalMaterialApi
@Composable
fun AccountSelectionDropDownField(selectedAccount: ContactAccount, onValueChanged: (ContactAccount) -> Unit) =
    AccountSelectionDropDownField(
        selectedAccount = selectedAccount,
        onValueChanged = onValueChanged,
    ) { options, selectedOption, onOptionSelected ->
        DropDownField(
            labelRes = R.string.target_account,
            selectedOption = selectedOption,
            options = options,
        ) { newValue -> onOptionSelected(newValue) }
    }

@ExperimentalMaterialApi
@Composable
fun AccountSelectionDropDownField(
    selectedAccount: ContactAccount,
    settings: ISettingsState = Settings.current,
    onValueChanged: (ContactAccount) -> Unit,
    dropDownFieldProvider: ContactAccountDropDownFieldProvider,
) {
    val options = settings.showThirdPartyContactAccounts.let { remember(it) { getAccountOptions(it) } }
    val selectedOption: DropDownOption<ContactAccount>? = options
        .firstOrNull { it.value == selectedAccount }
        ?: options.firstOrNull()?.also {
            selectedAccount.logger.info("Selected option not found: changing to ${it.value.type}")
            onValueChanged(it.value)
        }

    selectedOption?.let { dropDownFieldProvider(options, selectedOption, onValueChanged) }
        ?: selectedAccount.logger.warning("No options found for account selection drop-down")
}

private fun getAccountOptions(showThirdPartyAccounts: Boolean): List<DropDownOption<ContactAccount>> {
    val accountService: AccountService = getAnywhere()
    val accounts = accountService.loadAvailableAccounts(showThirdPartyAccounts)
    return accounts.map {
        DynamicStringDropDownOption(
            labelProvider = {
                val context = LocalContext.current
                it.getDisplayName(stringProvider = context::getString)
            },
            value = it
        )
    }
}

internal typealias ContactAccountDropDownFieldProvider = DropDownFieldProvider<ContactAccount>
