package ch.abwesend.privatecontacts.view.components.inputs.helper

import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.view.model.DropDownOption

internal typealias DropDownFieldProvider =
    @Composable (
        options: List<DropDownOption<ContactAccount>>,
        selectedOption: DropDownOption<ContactAccount>,
        onOptionSelected: (ContactAccount) -> Unit
    ) -> Unit
