/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.view.components.inputs.DropDownField
import ch.abwesend.privatecontacts.view.model.StringDropDownOption
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.ContactCategory
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.secondaryIconModifier
import ch.abwesend.privatecontacts.view.screens.contactedit.components.ContactEditCommonComponents.textFieldModifier
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.bringIntoViewDelayed
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import ch.abwesend.privatecontacts.view.util.createKeyboardAndFocusManager
import ch.abwesend.privatecontacts.view.util.getTitle
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
object ContactDataEditCommonComponents {
    private val parent = ContactEditScreen

    @Composable
    inline fun <reified T : StringBasedContactData<T>> ContactDataCategory(
        contact: IContactEditable,
        @StringRes categoryTitle: Int,
        @StringRes fieldLabel: Int,
        icon: ImageVector,
        keyboardType: KeyboardType,
        showIfEmpty: Boolean,
        initiallyExpanded: Boolean = false,
        noinline factory: (sortOrder: Int) -> T,
        noinline waitForCustomType: (ContactData) -> Unit,
        crossinline onChanged: @DisallowComposableCalls (IContactEditable) -> Unit,
    ) {
        val onEntryChanged: (T) -> Unit = remember(contact) {
            { newEntry ->
                contact.contactDataSet.addOrReplace(newEntry)
                onChanged(contact)
            }
        }

        val dataEntriesToDisplay = remember(contact) {
            contact.contactDataForDisplay(factory = factory)
        }
        val somethingToShow = remember(dataEntriesToDisplay) {
            dataEntriesToDisplay.any { !it.isEmpty }.also { showCategory ->
                if (!showCategory) {
                    logger.debug("Hiding category '${T::class.java.simpleName}'")
                }
            }
        }

        if (showIfEmpty || somethingToShow) {
            ContactCategory(
                categoryTitle = categoryTitle,
                icon = icon,
                initiallyExpanded = initiallyExpanded,
            ) {
                Column {
                    dataEntriesToDisplay.forEachIndexed { displayIndex, contactData ->
                        StringBasedContactDataEntry(
                            contactData = contactData,
                            label = fieldLabel,
                            keyboardType = keyboardType,
                            isLastElement = (displayIndex == dataEntriesToDisplay.size - 1),
                            waitForCustomType = waitForCustomType,
                            onChanged = onEntryChanged,
                        )
                        if (displayIndex < dataEntriesToDisplay.size - 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun <T : StringBasedContactData<T>> StringBasedContactDataEntry(
        contactData: T,
        @StringRes label: Int,
        keyboardType: KeyboardType,
        isLastElement: Boolean,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (T) -> Unit,
    ) {
        val manager = createKeyboardAndFocusManager()

        val viewRequester = remember { BringIntoViewRequester() }
        val scope = rememberCoroutineScope()

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.bringIntoViewRequester(viewRequester)
            ) {
                OutlinedTextField(
                    label = { Text(stringResource(id = label)) },
                    value = contactData.value,
                    singleLine = true,
                    onValueChange = { onChanged(contactData.changeValue(it)) },
                    modifier = textFieldModifier
                        .weight(1.0f)
                        .onFocusChanged {
                            if (it.isFocused) {
                                scope.launch {
                                    viewRequester.bringIntoViewDelayed()
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = keyboardType,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        manager.closeKeyboardAndClearFocus()
                    }),
                )

                Box(modifier = secondaryIconModifier) {
                    if (!isLastElement) {
                        IconButton(onClick = { onChanged(contactData.delete()) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                modifier = secondaryIconModifier,
                                contentDescription = stringResource(id = R.string.remove)
                            )
                        }
                    }
                }
            }

            ContactDataTypeDropDown(data = contactData, waitForCustomType) { newType ->
                onChanged(contactData.changeType(newType))
            }
        }
    }

    @Composable
    fun ContactDataTypeDropDown(
        data: ContactData,
        waitForCustomType: (ContactData) -> Unit,
        onChanged: (ContactDataType) -> Unit,
    ) {
        val context = LocalContext.current
        val selectedOption = with(data.type) {
            StringDropDownOption(label = getTitle(context), value = this)
        }
        val options = data.allowedTypes.map {
            StringDropDownOption(
                label = it.getTitle(context),
                value = it
            )
        }
        DropDownField(
            labelRes = R.string.type,
            selectedOption = selectedOption,
            options = options,
            isScrolling = { parent.isScrolling },
        ) { newValue ->
            if (newValue == ContactDataType.Custom) {
                waitForCustomType(data)
            } else {
                onChanged(newValue)
            }
        }
    }
}
