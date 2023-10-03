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
            isScrolling = { false },
        ) { newValue -> onOptionSelected(newValue) }
    }

@ExperimentalMaterialApi
@Composable
fun AccountSelectionDropDownField(
    selectedAccount: ContactAccount,
    onValueChanged: (ContactAccount) -> Unit,
    dropDownFieldProvider: ContactAccountDropDownFieldProvider,
) {
    val options = remember { getAccountOptions() }
    val selectedOption: DropDownOption<ContactAccount> = options
        .firstOrNull { it.value == selectedAccount }
        ?: options.first().also {
            selectedAccount.logger.info("Selected option not found: changing to ${it.value.type}")
            onValueChanged(it.value)
        }

    dropDownFieldProvider(options, selectedOption, onValueChanged)
}

private fun getAccountOptions(): List<DropDownOption<ContactAccount>> {
    val accountService: AccountService = getAnywhere()
    val accounts = accountService.loadAvailableAccounts()
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
