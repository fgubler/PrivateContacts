package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.theme.AppColors

@Composable
fun ContactEditContent(screenContext: ScreenContext, contact: ContactFull) {
    val onChanged = { newContact: ContactFull -> screenContext.contactEditViewModel.updateContact(newContact) }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        PersonalInformation(contact, onChanged)

        Notes(contact, onChanged)
    }
}

@Composable
private fun PersonalInformation(contact: ContactFull, onChanged: (ContactFull) -> Unit) {
    val textFieldModifier = Modifier.padding(bottom = 2.dp)
    ContactCategory(label = R.string.personal_information, icon = Icons.Default.Person) {
        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.first_name)) },
                value = contact.firstName,
                onValueChange = { newValue ->
                    onChanged(contact.copy(firstName = newValue))
                },
                singleLine = true,
                modifier = textFieldModifier,
            )
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.last_name)) },
                value = contact.lastName,
                onValueChange = { newValue ->
                    onChanged(contact.copy(lastName = newValue))
                },
                singleLine = true,
                modifier = textFieldModifier,
            )
        }
    }
}

@Composable
private fun Notes(contact: ContactFull, onChanged: (ContactFull) -> Unit) {
    ContactCategory(label = R.string.notes, icon = Icons.Default.SpeakerNotes) {
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.notes)) },
            value = contact.notes,
            onValueChange = { newValue ->
                onChanged(contact.copy(notes = newValue))
            },
            singleLine = false,
            maxLines = 10
        )
    }
}

@Composable
private fun ContactCategory(
    @StringRes label: Int,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = label),
            modifier = Modifier.padding(top = 23.dp, end = 20.dp),
            tint = AppColors.grayText
        )
        content()
    }
}
