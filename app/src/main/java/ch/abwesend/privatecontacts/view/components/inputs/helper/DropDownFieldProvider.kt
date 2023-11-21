package ch.abwesend.privatecontacts.view.components.inputs.helper

import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.view.model.DropDownOption

internal typealias DropDownFieldProvider<T> =
    @Composable (
        options: List<DropDownOption<T>>,
        selectedOption: DropDownOption<T>,
        onOptionSelected: (T) -> Unit
    ) -> Unit
