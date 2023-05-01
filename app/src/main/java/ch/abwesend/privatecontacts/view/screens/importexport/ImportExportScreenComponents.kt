/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.components.text.SectionTitle

object ImportExportScreenComponents {

    @Composable
    fun ImportExportCategory(@StringRes title: Int, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier
                .padding(all = 5.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SectionTitle(titleRes = title, addTopPadding = false)
                content()
            }
        }
    }
}
