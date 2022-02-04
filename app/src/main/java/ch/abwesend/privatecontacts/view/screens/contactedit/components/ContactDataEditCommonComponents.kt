package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import ch.abwesend.privatecontacts.view.util.getTitle

@ExperimentalMaterialApi
@Composable
inline fun <reified T : StringBasedContactData<T>> ContactEditScreen.ContactDataCategory(
    contact: IContactEditable,
    @StringRes categoryTitle: Int,
    @StringRes fieldLabel: Int,
    icon: ImageVector,
    keyboardType: KeyboardType,
    initiallyExpanded: Boolean = false,
    noinline factory: (sortOrder: Int) -> T,
    noinline waitForCustomType: (ContactData) -> Unit,
    crossinline onChanged: (IContactEditable) -> Unit,
) {
    val onEntryChanged: (T) -> Unit = { newEntry ->
        contact.contactDataSet.addOrReplace(newEntry)
        onChanged(contact)
    }

    ContactCategory(
        categoryTitle = categoryTitle,
        icon = icon,
        initiallyExpanded = initiallyExpanded,
    ) {
        val dataEntriesToDisplay = contact.contactDataForDisplay(factory)
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

@ExperimentalMaterialApi
@Composable
fun <T : StringBasedContactData<T>> ContactEditScreen.StringBasedContactDataEntry(
    contactData: T,
    @StringRes label: Int,
    keyboardType: KeyboardType,
    isLastElement: Boolean,
    waitForCustomType: (ContactData) -> Unit,
    onChanged: (T) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                label = { Text(stringResource(id = label)) },
                value = contactData.value,
                singleLine = true,
                onValueChange = { onChanged(contactData.changeValue(it)) },
                modifier = textFieldModifier.weight(1.0f),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Next
                ),
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

@ExperimentalMaterialApi
@Composable
fun ContactEditScreen.ContactDataTypeDropDown(
    data: ContactData,
    waitForCustomType: (ContactData) -> Unit,
    onChanged: (ContactDataType) -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
        modifier = Modifier.widthIn(min = 100.dp, max = 200.dp)
    ) {
        val context = LocalContext.current
        OutlinedTextField(
            label = { Text(stringResource(id = R.string.type)) },
            value = data.type.getTitle(context),
            readOnly = true,
            onValueChange = { }, // read-only...
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
            },
        )
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            data.allowedTypes.forEach { type ->
                DropdownMenuItem(
                    onClick = {
                        if (type == ContactDataType.Custom) {
                            waitForCustomType(data)
                        } else {
                            onChanged(type)
                        }
                        dropdownExpanded = false
                    }
                ) {
                    Text(text = type.getTitle(context))
                }
            }
        }
    }
}
