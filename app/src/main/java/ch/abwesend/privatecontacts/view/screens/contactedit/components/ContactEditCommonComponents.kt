/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
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
        val onExpandedChanged: (Boolean) -> Unit = { expanded = it }

        Card(modifier = modifier.padding(all = 5.dp)) {
            Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)) {
                Column {
                    ContactCategoryHeader(
                        title = categoryTitle,
                        icon = icon,
                        expanded = expanded,
                        onToggleExpanded = { onExpandedChanged(!expanded) }
                    )
                    if (expanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (alignContentWithTitle) {
                                Spacer(modifier = primaryIconModifier.padding(end = iconHorizontalPadding))
                            } else {
                                Spacer(modifier = Modifier.padding(end = 5.dp))
                            }
                            Box(modifier = Modifier.padding(end = 5.dp)) {
                                content()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ContactCategoryHeader(
        @StringRes title: Int,
        icon: ImageVector,
        expanded: Boolean,
        onToggleExpanded: () -> Unit,
    ) {
        val expandIcon = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded() }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = title),
                modifier = primaryIconModifier.padding(end = iconHorizontalPadding),
                tint = AppColors.greyText
            )
            Column(modifier = Modifier.weight(1.0f)) {
                Text(text = stringResource(id = title))
            }
            Icon(
                imageVector = expandIcon,
                contentDescription = stringResource(id = R.string.expand),
                modifier = secondaryIconModifier.padding(start = iconHorizontalPadding)
            )
        }
    }
}
