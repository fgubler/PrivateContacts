/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.getFullName

object ContactDetailScreenContent {
    @Composable
    fun ScreenContent(contact: IContact) {
        Text(
            text = contact.getFullName(),
            modifier = Modifier.padding(start = 20.dp),
            style = MaterialTheme.typography.h4
        )
        // TODO implement
    }
}
