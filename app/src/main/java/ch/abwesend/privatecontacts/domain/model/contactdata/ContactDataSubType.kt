package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.BIRTHDAY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.MOBILE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.OTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType.Key.PRIVATE
import ch.abwesend.privatecontacts.domain.util.StringProvider

sealed class ContactDataSubType {
    abstract val key: Key
    protected abstract val titleRes: Int
    protected open val isPlaceHolder: Boolean = false

    open fun getTitle(stringProvider: StringProvider): String =
        stringProvider(titleRes)

    object Mobile : ContactDataSubType() {
        override val key: Key = MOBILE
        override val titleRes: Int = R.string.subtype_mobile
    }

    object Private : ContactDataSubType() {
        override val key: Key = PRIVATE
        override val titleRes: Int = R.string.subtype_private
    }

    object Business : ContactDataSubType() {
        override val key: Key = BUSINESS
        override val titleRes: Int = R.string.subtype_business
    }

    object Other : ContactDataSubType() {
        override val key: Key = OTHER
        override val titleRes: Int = R.string.subtype_other
    }

    object Birthday : ContactDataSubType() {
        override val key: Key = BIRTHDAY
        override val titleRes: Int = R.string.subtype_birthday
    }

    /** The "Custom" which is shown in the dropdown of possible sub-types */
    object Custom : ContactDataSubType() {
        override val key: Key = CUSTOM
        override val titleRes: Int = R.string.subtype_custom
        override val isPlaceHolder: Boolean = true
    }

    /** The "Custom" which is then created based on [customValue] */
    class CustomValue(val customValue: String) : ContactDataSubType() {
        override val key: Key = CUSTOM
        override val titleRes: Int = R.string.subtype_custom
        override fun getTitle(stringProvider: StringProvider): String = customValue
    }

    enum class Key {
        PRIVATE,
        BUSINESS,
        MOBILE,
        OTHER,
        BIRTHDAY,
        CUSTOM,
    }
}
