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
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.theme.selectedElement

private const val EASTER_EGG_LOVE = "love"

@ExperimentalFoundationApi
@Composable
fun ContactList(
    pagedContacts: LazyPagingItems<IContactBase>,
    selectedContacts: Set<ContactId>,
    onContactClicked: (IContactBase) -> Unit,
    onContactLongClicked: (IContactBase) -> Unit,
) {
    if (pagedContacts.itemCount <= 0) {
        NoResults()
    } else {
        ListOfContacts(
            pagedContacts = pagedContacts,
            selectedContacts = selectedContacts,
            onContactClicked = onContactClicked,
            onContactLongClicked = onContactLongClicked,
        )
    }
}

@Composable
private fun NoResults() {
    FullScreenError(errorMessage = R.string.no_contacts_found)
}

@ExperimentalFoundationApi
@Composable
private fun ListOfContacts(
    pagedContacts: LazyPagingItems<IContactBase>,
    selectedContacts: Set<ContactId>,
    onContactClicked: (IContactBase) -> Unit,
    onContactLongClicked: (IContactBase) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        items(pagedContacts) { nullableContact ->
            val selected = nullableContact?.let { selectedContacts.contains(it.id) } ?: false
            Contact(
                nullableContact = nullableContact,
                selected = selected,
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
    nullableContact: IContactBase?,
    selected: Boolean,
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
                onClick = { nullableContact?.let { onClicked(it) } },
                onLongClick = { nullableContact?.let { onLongClicked(it) } }
            )
    ) {
        nullableContact?.let { contact ->
            val name = contact.getFullName()
            val icon = when {
                selected -> Icons.Default.TaskAlt
                name.lowercase().contains(EASTER_EGG_LOVE) -> Icons.Filled.Favorite
                else -> Icons.Filled.AccountCircle
            }
            Icon(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier.padding(start = 10.dp, end = 20.dp)
            )
            Text(text = name)
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}
