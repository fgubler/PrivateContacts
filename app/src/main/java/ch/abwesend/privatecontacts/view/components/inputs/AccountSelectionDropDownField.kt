/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.service.interfaces.AccountService
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.view.model.DropDownOption
import ch.abwesend.privatecontacts.view.model.DynamicStringDropDownOption

/** Dropdown to select from the available accounts on the device into which contacts can be saved */
@ExperimentalMaterialApi
@Composable
fun AccountSelectionDropDownField(onValueChanged: (ContactAccount) -> Unit) {
    val options = remember { getAccountOptions() }
    var selectedOption: DropDownOption<ContactAccount> by remember { mutableStateOf(options.first()) }

    DropDownField(
        labelRes = R.string.target_account,
        selectedOption = selectedOption,
        options = options,
        isScrolling = { false },
    ) { newValue ->
        options.firstOrNull { it.value == newValue }?.let { selectedOption = it }
        onValueChanged(newValue)
    }
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