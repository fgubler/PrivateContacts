/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBaseWithAccountInformation
import ch.abwesend.privatecontacts.view.components.inputs.AccountSelectionDropDownField

sealed interface ContactTypeChangeMenuConfig {
    val targetType: ContactType

    val menuTextSingularRes: Int
    val menuTextPluralRes: Int
    val confirmationDialogTitleSingularRes: Int
    val confirmationDialogTitlePluralRes: Int
    val confirmationDialogTextRes: Int

    @Composable
    fun ConfirmationDialogAdditionalContent(
        contacts: Collection<IContactBaseWithAccountInformation>,
        changeSaveButtonState: (enabled: Boolean) -> Unit,
    ) {}

    @ExperimentalMaterialApi
    companion object {
        fun fromTargetType(targetType: ContactType): ContactTypeChangeMenuConfig =
            when (targetType) {
                ContactType.SECRET -> ContactTypeChangeToSecretMenuConfig
                ContactType.PUBLIC -> ContactTypeChangeToPublicMenuConfig
            }
    }
}

object ContactTypeChangeToSecretMenuConfig : ContactTypeChangeMenuConfig {
    override val targetType: ContactType = ContactType.SECRET

    @StringRes override val menuTextSingularRes: Int = R.string.make_contact_secret

    @StringRes override val menuTextPluralRes: Int = R.string.make_contacts_secret

    @StringRes override val confirmationDialogTitleSingularRes: Int = R.string.make_contact_secret_title

    @StringRes override val confirmationDialogTitlePluralRes: Int = R.string.make_contacts_secret_title

    @StringRes override val confirmationDialogTextRes: Int = R.string.make_contact_secret_text
}

@ExperimentalMaterialApi
object ContactTypeChangeToPublicMenuConfig : ContactTypeChangeMenuConfig {
    override val targetType: ContactType = ContactType.PUBLIC

    @StringRes override val menuTextSingularRes: Int = R.string.make_contact_public

    @StringRes override val menuTextPluralRes: Int = R.string.make_contacts_public

    @StringRes override val confirmationDialogTitleSingularRes: Int = R.string.make_contact_public_title

    @StringRes override val confirmationDialogTitlePluralRes: Int = R.string.make_contacts_public_title

    @StringRes override val confirmationDialogTextRes: Int = R.string.make_contact_public_text

    @Composable
    override fun ConfirmationDialogAdditionalContent(
        contacts: Collection<IContactBaseWithAccountInformation>,
        changeSaveButtonState: (enabled: Boolean) -> Unit,
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        val defaultAccount = ContactAccount.currentDefaultForContactType(targetType)
        contacts.forEach { it.saveInAccount = defaultAccount } // needed if the user just clicks "ok"
        AccountSelectionDropDownField(defaultAccount) { newValue ->
            changeSaveButtonState(false)
            contacts.forEach { it.saveInAccount = newValue }
            changeSaveButtonState(true)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "More options will be added in the future...", fontStyle = FontStyle.Italic)
    }
}
