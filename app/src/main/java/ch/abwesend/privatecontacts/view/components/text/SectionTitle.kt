/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.text

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(@StringRes titleRes: Int, addTopPadding: Boolean = true) {
    SectionTitleLike(
        titleRes = titleRes,
        style = MaterialTheme.typography.headlineMedium,
        addTopPadding = addTopPadding,
    )
}

@Composable
fun SectionSubtitle(@StringRes titleRes: Int, addTopPadding: Boolean = true) {
    SectionTitleLike(
        titleRes = titleRes,
        style = MaterialTheme.typography.titleLarge,
        addTopPadding = addTopPadding,
    )
}

@Composable
private fun SectionTitleLike(
    @StringRes titleRes: Int,
    style: TextStyle,
    addTopPadding: Boolean = true
) {
    val topPadding = if (addTopPadding) 20.dp else 0.dp
    Text(
        text = stringResource(id = titleRes),
        style = style,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = topPadding, bottom = 5.dp)
    )
}
