package ch.abwesend.privatecontacts.view.screens.contactedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerNotes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.view.model.config.IconConfig
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfig
import ch.abwesend.privatecontacts.view.theme.AppColors

@Composable
fun ContactEditContent(screenContext: ScreenContext, contact: ContactFull) {
    val onChanged = { newContact: ContactFull -> screenContext.contactEditViewModel.changeContact(newContact) }
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
    val prefixIcon = IconConfig(label = R.string.personal_information, icon = Icons.Default.Person)
    ContactCategory(prefixIcon) {
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
    val prefixIcon = IconConfig(label = R.string.notes, icon = Icons.Default.SpeakerNotes)
    ContactCategory(prefixIcon = prefixIcon) {
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
    prefixIcon: IconConfig,
    postfixIcon: IconButtonConfig? = null,
    content: @Composable () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        val (prefixIconRef, contentRef, postfixIconRef) = createRefs()
        Icon(
            imageVector = prefixIcon.icon,
            contentDescription = stringResource(id = prefixIcon.label),
            tint = AppColors.grayText,
            modifier = Modifier
                .padding(top = 23.dp, end = 20.dp)
                .constrainAs(prefixIconRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
        )
        Surface(
            modifier = Modifier.constrainAs(contentRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(prefixIconRef.end)
            }
        ) {
            content()
        }
        postfixIcon?.let {
            IconButton(onClick = postfixIcon.onClick) {
                Icon(
                    imageVector = postfixIcon.icon,
                    contentDescription = stringResource(id = postfixIcon.label),
                    tint = AppColors.grayText,
                    modifier = Modifier
                        .padding(top = 23.dp, start = 20.dp)
                        .constrainAs(postfixIconRef) {
                            top.linkTo(parent.top)
                            start.linkTo(contentRef.end)
                        },
                )
            }
        }
    }
}
