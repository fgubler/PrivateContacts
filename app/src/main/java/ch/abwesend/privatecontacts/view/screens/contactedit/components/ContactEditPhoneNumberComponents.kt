package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.phoneNumbersForDisplay

@ExperimentalMaterialApi
@Composable
fun ContactEditScreen.PhoneNumbers(
    contact: IContactEditable,
    waitForCustomType: (ContactData) -> Unit,
    onChanged: (IContactEditable) -> Unit,
) {
    val onPhoneNumberChanged: (PhoneNumber) -> Unit = { newNumber ->
        contact.contactDataSet.addOrReplace(newNumber)
        onChanged(contact)
    }

    val phoneNumbersToDisplay = contact.phoneNumbersForDisplay
    ContactCategory(label = R.string.phone_number, icon = Icons.Default.Phone) {
        Column {
            phoneNumbersToDisplay.forEachIndexed { displayIndex, phoneNumber ->
                StringBasedContactDataEntry(
                    contactData = phoneNumber,
                    isLastElement = (displayIndex == phoneNumbersToDisplay.size - 1),
                    waitForCustomType = waitForCustomType,
                    onChanged = onPhoneNumberChanged,
                )
                if (displayIndex < phoneNumbersToDisplay.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
