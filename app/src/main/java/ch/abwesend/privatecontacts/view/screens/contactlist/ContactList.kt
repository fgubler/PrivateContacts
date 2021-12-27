package ch.abwesend.privatecontacts.view.screens.contactlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.Contact
import ch.abwesend.privatecontacts.domain.model.getFullName

@Composable
fun ContactList(
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
) {
    if (contacts.isEmpty()) {
        NoResults()
    } else {
        ListWithResults(
            contacts = contacts,
            onContactSelected = onContactSelected
        )
    }
}

@Composable
private fun NoResults() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(text = stringResource(id = R.string.no_contacts_found))
    }
}

@Composable
private fun ListWithResults(
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        items(contacts) { contact ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { onContactSelected(contact) }
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = contact.getFullName(firstNameFirst = true),
                    modifier = Modifier.padding(start = 10.dp, end = 20.dp)
                )
                Text(text = contact.getFullName(firstNameFirst = true))
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
