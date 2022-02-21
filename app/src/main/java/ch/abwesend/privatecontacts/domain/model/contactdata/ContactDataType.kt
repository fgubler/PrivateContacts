/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BIRTHDAY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MAIN
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.OTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PRIVATE
import ch.abwesend.privatecontacts.domain.util.StringProvider

sealed class ContactDataType {
    abstract val key: Key
    protected abstract val titleRes: Int
    protected open val isPlaceHolder: Boolean = false

    open fun getTitle(stringProvider: StringProvider): String =
        stringProvider(titleRes)

    object Mobile : ContactDataType() {
        override val key: Key = MOBILE
        override val titleRes: Int = R.string.type_mobile
    }

    object Private : ContactDataType() {
        override val key: Key = PRIVATE
        override val titleRes: Int = R.string.type_private
    }

    object Business : ContactDataType() {
        override val key: Key = BUSINESS
        override val titleRes: Int = R.string.type_business
    }

    object Other : ContactDataType() {
        override val key: Key = OTHER
        override val titleRes: Int = R.string.type_other
    }

    object Birthday : ContactDataType() {
        override val key: Key = BIRTHDAY
        override val titleRes: Int = R.string.type_birthday
    }

    object Main : ContactDataType() {
        override val key: Key = MAIN
        override val titleRes: Int = R.string.type_main
    }

    /** The "Custom" which is shown in the dropdown of possible sub-types */
    object Custom : ContactDataType() {
        override val key: Key = CUSTOM
        override val titleRes: Int = R.string.type_custom
        override val isPlaceHolder: Boolean = true
    }

    /** The "Custom" which is then created based on [customValue] */
    class CustomValue(val customValue: String) : ContactDataType() {
        override val key: Key = CUSTOM
        override val titleRes: Int = R.string.type_custom
        override fun getTitle(stringProvider: StringProvider): String = customValue
    }

    enum class Key {
        PRIVATE,
        BUSINESS,
        MOBILE,
        OTHER,
        BIRTHDAY,
        MAIN,
        CUSTOM,
    }
}
