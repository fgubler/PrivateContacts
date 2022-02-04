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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.contactDataForDisplay
import ch.abwesend.privatecontacts.view.util.getTitle

val textFieldModifier = Modifier.padding(bottom = 2.dp)

private val primaryIconModifier = Modifier.width(60.dp)
private val secondaryIconModifier = Modifier.width(40.dp)
private val iconHorizontalPadding = 20.dp

@Suppress("unused")
@Composable
fun ContactEditScreen.ContactCategory(
    @StringRes categoryTitle: Int,
    icon: ImageVector,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val onExpandChanged: (Boolean) -> Unit = { expanded = it }

    ContactDataCategoryHeader(
        title = categoryTitle,
        icon = icon,
        expanded = expanded,
        onExpand = onExpandChanged,
        content = content
    )
}

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
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val onExpandChanged: (Boolean) -> Unit = { expanded = it }

    val onEntryChanged: (T) -> Unit = { newEntry ->
        contact.contactDataSet.addOrReplace(newEntry)
        onChanged(contact)
    }

    ContactDataCategoryHeader(
        title = categoryTitle,
        icon = icon,
        expanded = expanded,
        onExpand = onExpandChanged
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

@Suppress("unused")
@Composable
fun ContactEditScreen.ContactDataCategoryHeader(
    @StringRes title: Int,
    icon: ImageVector,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val expandIcon = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
    Card(modifier = Modifier.padding(all = 5.dp)) {
        Box(modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpand(!expanded) }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(id = title),
                        modifier = primaryIconModifier.padding(end = iconHorizontalPadding),
                        tint = AppColors.grayText
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
                if (expanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = primaryIconModifier.padding(end = iconHorizontalPadding))
                        Box(modifier = Modifier.weight(1.0f)) {
                            content()
                        }
                        Spacer(modifier = secondaryIconModifier.padding(start = iconHorizontalPadding))
                    }
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
    Row {
        Column(modifier = Modifier.weight(1.0f)) {
            OutlinedTextField(
                label = { Text(stringResource(id = label)) },
                value = contactData.value,
                singleLine = true,
                onValueChange = { onChanged(contactData.changeValue(it)) },
                modifier = textFieldModifier,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Next
                ),
            )

            ContactDataTypeDropDown(data = contactData, waitForCustomType) { newType ->
                onChanged(contactData.changeType(newType))
            }
        }

        Box(modifier = secondaryIconModifier) {
            if (!isLastElement) {
                IconButton(onClick = { onChanged(contactData.delete()) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        modifier = secondaryIconModifier.padding(top = 23.dp),
                        contentDescription = stringResource(id = R.string.remove)
                    )
                }
            }
        }
    }
}

@Suppress("unused")
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
