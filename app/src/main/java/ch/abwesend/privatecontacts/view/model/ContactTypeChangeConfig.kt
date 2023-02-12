/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.ContactType

sealed interface ContactTypeChangeMenuConfig {
    val targetType: ContactType

    val menuTextSingularRes: Int
    val menuTextPluralRes: Int
    val confirmationDialogTitleSingularRes: Int
    val confirmationDialogTitlePluralRes: Int
    val confirmationDialogTextRes: Int

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

object ContactTypeChangeToPublicMenuConfig : ContactTypeChangeMenuConfig {
    override val targetType: ContactType = ContactType.PUBLIC

    @StringRes override val menuTextSingularRes: Int = R.string.make_contact_public
    @StringRes override val menuTextPluralRes: Int = R.string.make_contacts_public
    @StringRes override val confirmationDialogTitleSingularRes: Int = R.string.make_contact_public_title
    @StringRes override val confirmationDialogTitlePluralRes: Int = R.string.make_contacts_public_title
    @StringRes override val confirmationDialogTextRes: Int = R.string.make_contact_public_text
}
