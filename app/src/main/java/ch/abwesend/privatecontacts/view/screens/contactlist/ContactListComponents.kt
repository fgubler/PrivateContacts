/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.view.components.FullScreenError

@Composable
fun ContactList(
    pagedContacts: LazyPagingItems<IContactBase>,
    onContactSelected: (IContactBase) -> Unit,
) {
    if (pagedContacts.itemCount <= 0) {
        NoResults()
    } else {
        ListWithResults(
            pagedContacts = pagedContacts,
            onContactSelected = onContactSelected
        )
    }
}

@Composable
private fun NoResults() {
    FullScreenError(errorMessage = R.string.no_contacts_found)
}

@Composable
private fun ListWithResults(
    pagedContacts: LazyPagingItems<IContactBase>,
    onContactSelected: (IContactBase) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        items(pagedContacts) { nullableContact ->
            // Beware: Need to draw the row even for null, otherwise, loading new pages does not work properly
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { nullableContact?.let { onContactSelected(it) } }
            ) {
                nullableContact?.let { contact ->
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = contact.getFullName(),
                        modifier = Modifier.padding(start = 10.dp, end = 20.dp)
                    )
                    Text(text = contact.getFullName())
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
