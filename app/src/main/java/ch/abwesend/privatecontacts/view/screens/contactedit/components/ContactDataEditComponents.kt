package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.KeyboardType
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactDataEditCommonComponents.ContactDataCategory

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
object ContactDataEditComponents {

    @Composable
    fun PhoneNumbers(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = R.string.phone_numbers,
            fieldLabel = R.string.phone_number,
            icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            showIfEmpty = showIfEmpty,
            factory = { PhoneNumber.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun EmailAddresses(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = R.string.email_addresses,
            fieldLabel = R.string.email_address,
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            showIfEmpty = showIfEmpty,
            factory = { EmailAddress.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }

    @Composable
    fun PhysicalAddresses(
        contact: IContactEditable,
        showIfEmpty: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (IContactEditable) -> Unit,
    ) {
        ContactDataCategory(
            contact = contact,
            categoryTitle = R.string.physical_addresses,
            fieldLabel = R.string.physical_address,
            icon = Icons.Default.Home,
            keyboardType = KeyboardType.Text,
            showIfEmpty = showIfEmpty,
            factory = { PhysicalAddress.createEmpty(it) },
            waitForCustomType = waitForCustomType,
            onChanged = onChanged
        )
    }
}
