/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.text

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(@StringRes titleRes: Int, addTopPadding: Boolean = true) {
    val topPadding = if (addTopPadding) 20.dp else 0.dp
    Text(
        text = stringResource(id = titleRes),
        style = MaterialTheme.typography.h5,
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(top = topPadding, bottom = 5.dp)
    )
}
