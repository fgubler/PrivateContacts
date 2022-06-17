/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.theme.selectedElement
import ch.abwesend.privatecontacts.view.util.color

private const val EASTER_EGG_LOVE = "love"

@ExperimentalFoundationApi
@Composable
fun ContactList(
    contacts: List<IContactBase>,
    selectedContacts: Set<ContactId>,
    showTypeIcons: Boolean,
    onContactClicked: (IContactBase) -> Unit,
    onContactLongClicked: (IContactBase) -> Unit,
) {
    if (contacts.isEmpty()) NoResults()
    else ListOfContacts(
        contacts = contacts,
        selectedContacts = selectedContacts,
        showTypeIcons = showTypeIcons,
        onContactClicked = onContactClicked,
        onContactLongClicked = onContactLongClicked,
    )
}

@Composable
private fun NoResults() {
    FullScreenError(errorMessage = R.string.no_contacts_found)
}

@ExperimentalFoundationApi
@Composable
private fun ListOfContacts(
    contacts: List<IContactBase>,
    selectedContacts: Set<ContactId>,
    showTypeIcons: Boolean,
    onContactClicked: (IContactBase) -> Unit,
    onContactLongClicked: (IContactBase) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        items(contacts) { contact ->
            val selected = selectedContacts.contains(contact.id)
            Contact(
                contact = contact,
                selected = selected,
                showTypeIcon = showTypeIcons,
                onClicked = onContactClicked,
                onLongClicked = onContactLongClicked,
            )
        }
    }
}

/** Beware: Need to draw the row even for null, otherwise, loading new pages does not work properly */
@ExperimentalFoundationApi
@Composable
private fun Contact(
    contact: IContactBase,
    selected: Boolean,
    showTypeIcon: Boolean,
    onClicked: (IContactBase) -> Unit,
    onLongClicked: (IContactBase) -> Unit,
) {
    val background =
        if (selected) MaterialTheme.colors.selectedElement
        else MaterialTheme.colors.background

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(background)
            .combinedClickable(
                onClick = { onClicked(contact) },
                onLongClick = { onLongClicked(contact) }
            )
    ) {
        val name = contact.displayName
        val contactIcon = when {
            selected -> Icons.Default.TaskAlt
            name.lowercase().contains(EASTER_EGG_LOVE) -> Icons.Filled.Favorite
            else -> Icons.Filled.AccountCircle
        }

        Icon(
            imageVector = contactIcon,
            contentDescription = name,
            modifier = if (showTypeIcon) Modifier.padding(start = 10.dp)
            else Modifier.padding(start = 10.dp, end = 20.dp)
        )
        if (showTypeIcon) {
            Icon(
                imageVector = contact.type.icon,
                contentDescription = stringResource(id = contact.type.label),
                modifier = Modifier.padding(start = 5.dp, end = 20.dp),
                tint = contact.type.color,
            )
        }
        Text(text = name)
    }
    Spacer(modifier = Modifier.height(6.dp))
}
