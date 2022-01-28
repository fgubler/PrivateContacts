package ch.abwesend.privatecontacts.view.screens.contactedit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.view.screens.contactedit.ContactEditScreen
import ch.abwesend.privatecontacts.view.theme.AppColors
import ch.abwesend.privatecontacts.view.util.getTitle

val contactDataIconModifier = Modifier.padding(top = 23.dp)
val textFieldModifier = Modifier.padding(bottom = 2.dp)

@Composable
fun ContactEditScreen.ContactCategory(
    @StringRes label: Int,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = label),
            modifier = contactDataIconModifier.padding(end = 20.dp),
            tint = AppColors.grayText
        )
        content()
    }
}

@ExperimentalMaterialApi
@Composable
fun <T : StringBasedContactData<T>> ContactEditScreen.StringBasedContactDataEntry(
    contactData: T,
    isLastElement: Boolean,
    waitForCustomType: (ContactData) -> Unit,
    onChanged: (T) -> Unit,
) {
    Row {
        Column {
            OutlinedTextField(
                label = { Text(stringResource(id = R.string.phone_number)) },
                value = contactData.value,
                singleLine = true,
                onValueChange = { onChanged(contactData.changeValue(it)) },
                modifier = textFieldModifier,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
            )

            ContactDataTypeDropDown(data = contactData, waitForCustomType) { newType ->
                onChanged(contactData.changeType(newType))
            }
        }
        if (!isLastElement) {
            IconButton(onClick = { onChanged(contactData.delete()) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    modifier = contactDataIconModifier,
                    contentDescription = stringResource(id = R.string.remove)
                )
            }
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
