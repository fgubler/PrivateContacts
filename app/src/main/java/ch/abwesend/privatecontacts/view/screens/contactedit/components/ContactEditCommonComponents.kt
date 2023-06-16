/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.view.components.ExpandableCard
import ch.abwesend.privatecontacts.view.theme.AppColors

object ContactEditCommonComponents {
    private val primaryIconModifier = Modifier.width(60.dp)
    private val iconHorizontalPadding = 20.dp

    val secondaryIconModifier = Modifier.width(40.dp)
    val textFieldModifier = Modifier.padding(bottom = 2.dp)

    @Composable
    fun ContactCategory(
        @StringRes categoryTitle: Int,
        icon: ImageVector,
        modifier: Modifier = Modifier,
        initiallyExpanded: Boolean = true,
        alignContentWithTitle: Boolean = true,
        content: @Composable () -> Unit
    ) {
        var expanded by remember { mutableStateOf(initiallyExpanded) }

        ExpandableCard(
            expanded = expanded,
            modifier = modifier,
            onToggleExpanded = { expanded = it },
            header = { ContactCategoryHeader(title = categoryTitle, icon = icon) }
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (alignContentWithTitle) {
                    Spacer(modifier = primaryIconModifier.padding(end = iconHorizontalPadding))
                } else {
                    Spacer(modifier = Modifier.padding(end = 5.dp))
                }
                Surface(modifier = Modifier.padding(end = 5.dp)) {
                    content()
                }
            }
        }
    }

    @Composable
    fun ContactCategoryHeader(
        @StringRes title: Int,
        icon: ImageVector,
    ) {
        Row {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = title),
                modifier = primaryIconModifier.padding(end = iconHorizontalPadding),
                tint = AppColors.greyText
            )
            Column(modifier = Modifier.weight(1.0f)) {
                Text(text = stringResource(id = title))
            }
        }
    }
}
