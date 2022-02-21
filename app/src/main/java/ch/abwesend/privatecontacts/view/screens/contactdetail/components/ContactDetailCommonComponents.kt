/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfigGeneric
import ch.abwesend.privatecontacts.view.model.config.IconConfig
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import ch.abwesend.privatecontacts.view.util.getTitle
import ch.abwesend.privatecontacts.view.util.toIconButton

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactDetailCommonComponents {
    private val primaryIconModifier = Modifier.width(40.dp)
    private val iconHorizontalPadding = 10.dp

    @Composable
    fun labelColor() = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)

    @Composable
    fun ContactCategoryWithoutHeader(
        iconConfig: IconConfig,
        content: @Composable () -> Unit
    ) {
        Card(modifier = Modifier.padding(all = 5.dp)) {
            Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)) {
                Row {
                    Icon(
                        imageVector = iconConfig.icon,
                        contentDescription = stringResource(id = iconConfig.label),
                        modifier = primaryIconModifier.padding(top = 16.dp),
                    )
                    Box(modifier = Modifier.padding(horizontal = 5.dp).fillMaxWidth()) {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    fun ContactCategoryWithHeader(
        @StringRes categoryTitle: Int,
        icon: ImageVector,
        alignContentWithTitle: Boolean,
        content: @Composable () -> Unit
    ) {
        Card(modifier = Modifier.padding(all = 5.dp)) {
            Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)) {
                Column {
                    ContactCategoryHeader(
                        title = categoryTitle,
                        icon = icon,
                    )
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

    @Composable
    fun ContactCategoryHeader(
        @StringRes title: Int,
        icon: ImageVector,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = title),
                modifier = primaryIconModifier.padding(end = iconHorizontalPadding),
                tint = AppColors.grayText
            )
            Column(modifier = Modifier.weight(1.0f)) {
                Text(text = stringResource(id = title))
            }
        }
    }

    @Composable
    inline fun <reified T: ContactData> ContactDataCategory(
        contact: IContact,
        iconConfig: IconConfig,
        secondaryActionConfig: IconButtonConfigGeneric<String>? = null,
        noinline factory: (sortOrder: Int) -> T,
        noinline primaryAction: () -> Unit,
    ) {
        val data = contact.contactDataForDisplay(addEmptyElement = false, factory = factory)

        if (data.isNotEmpty()) {
            ContactCategoryWithoutHeader(iconConfig = iconConfig) {
                Column {
                    data.forEach { element ->
                        ContactDataRow(
                            primaryText = element.displayValue,
                            secondaryText = element.type.getTitle(),
                            primaryAction = primaryAction,
                            secondaryActionConfig = secondaryActionConfig,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ContactDataRow(
        primaryText: String,
        secondaryText: String?,
        primaryAction: () -> Unit,
        secondaryActionConfig: IconButtonConfigGeneric<String>?
    ) {
        val labelStyle = LocalTextStyle.current.copy(
            fontSize = LocalTextStyle.current.fontSize.times(0.8),
            color = labelColor(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .clickable { primaryAction() }
                .padding(start = iconHorizontalPadding)
                .padding(vertical = 5.dp)
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Text(text = primaryText)
                secondaryText?.let {
                    Text(text = it, style = labelStyle)
                }
            }
            secondaryActionConfig?.toIconButton(primaryText)
        }
    }
}
