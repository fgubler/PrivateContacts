/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.ContactCategory
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.secondaryIconModifier
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.textFieldModifier
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import ch.abwesend.privatecontacts.view.util.getTitle

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactDetailCommonComponents {
    private val primaryIconModifier = Modifier.width(40.dp)
    private val iconHorizontalPadding = 10.dp

    @Composable
    fun ContactCategoryWithoutHeader(
        @StringRes label: Int,
        icon: ImageVector,
        content: @Composable () -> Unit
    ) {
        Card(modifier = Modifier.padding(all = 5.dp)) {
            Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)) {
                Row {
                    Icon(imageVector = icon, contentDescription = stringResource(id = label))
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(horizontal = 5.dp)) {
                                content()
                            }
                        }
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
}
