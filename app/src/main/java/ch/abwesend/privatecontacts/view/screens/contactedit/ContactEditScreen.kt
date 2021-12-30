package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.view.components.ButtonConfig
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.buttons.CancelIconButton
import ch.abwesend.privatecontacts.view.components.buttons.SaveIconButton
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel

@Composable
fun ContactEditScreen(screenContext: ScreenContext) {
    val viewModel = screenContext.contactEditViewModel
    val selectedContact: ContactEditable? by viewModel.contact

    selectedContact?.let { contact ->
        Scaffold(
            topBar = { ContactEditTopBar(screenContext, contact) }
        ) {
            ContactEditContent(screenContext, contact)
        }
    } ?: NoContactLoaded(viewModel)
}

@Composable
private fun ContactEditTopBar(screenContext: ScreenContext, contact: ContactEditable) {
    @StringRes val title = if (contact.isNew) R.string.screen_contact_edit_create
    else R.string.screen_contact_edit

    TopAppBar(
        title = { Text(text = stringResource(id = title)) },
        navigationIcon = {
            CancelIconButton { onCancel(screenContext) }
        },
        actions = {
            SaveIconButton { onSave(screenContext, contact) }
        }
    )
}

private fun onSave(screenContext: ScreenContext, contact: ContactEditable) {
    screenContext.contactEditViewModel.saveContact(contact)
    screenContext.router.navigateUp()
}

private fun onCancel(screenContext: ScreenContext) {
    // TODO ask the user for confirmation
    screenContext.contactEditViewModel.clearContact()
    screenContext.router.navigateUp()
}

@Composable
private fun NoContactLoaded(viewModel: ContactEditViewModel) {
    FullScreenError(
        errorMessage = R.string.no_contact_selected,
        buttonConfig = ButtonConfig(
            label = R.string.create_contact,
            icon = Icons.Default.Add
        ) {
            viewModel.createNewContact()
        }
    )
}

@Composable
private fun ContactEditContent(screenContext: ScreenContext, contact: ContactEditable) {
    PersonalInformation(contact)
}

@Composable
private fun PersonalInformation(contact: ContactEditable) {
    val textFieldModifier = Modifier.padding(bottom = 2.dp)
    ContactCategory(label = R.string.personal_information, icon = Icons.Default.Person) {
        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.first_name)) },
                value = contact.firstName,
                onValueChange = { contact.firstName = it },
                modifier = textFieldModifier,
            )
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.last_name)) },
                value = contact.lastName,
                onValueChange = { contact.lastName = it },
                modifier = textFieldModifier,
            )
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.nickname)) },
                value = contact.nickname,
                onValueChange = { contact.nickname = it },
                modifier = textFieldModifier,
            )
        }
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
