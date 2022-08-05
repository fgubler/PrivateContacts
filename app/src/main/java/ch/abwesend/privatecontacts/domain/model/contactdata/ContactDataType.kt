/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.ANNIVERSARY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BIRTHDAY
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.CUSTOM
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MAIN
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.MOBILE_BUSINESS
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.OTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.PERSONAL
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_PARENT
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_PARTNER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_RELATIVE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_SIBLING
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_WORK
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

    object Personal : ContactDataType() {
        override val key: Key = PERSONAL
        override val titleRes: Int = R.string.type_personal
    }

    object Business : ContactDataType() {
        override val key: Key = BUSINESS
        override val titleRes: Int = R.string.type_business
    }

    object MobileBusiness : ContactDataType() {
        override val key: Key = MOBILE_BUSINESS
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

    object Anniversary : ContactDataType() {
        override val key: Key = ANNIVERSARY
        override val titleRes: Int = R.string.type_anniversary
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

    object RelationshipSibling : ContactDataType() {
        override val key: Key = RELATIONSHIP_SIBLING
        override val titleRes: Int = R.string.type_relationship_sibling
    }

    object RelationshipParent : ContactDataType() {
        override val key: Key = RELATIONSHIP_PARENT
        override val titleRes: Int = R.string.type_relationship_parent
    }

    object RelationshipChild : ContactDataType() {
        override val key: Key = Key.RELATIONSHIP_CHILD
        override val titleRes: Int = R.string.type_relationship_child
    }

    object RelationshipPartner : ContactDataType() {
        override val key: Key = RELATIONSHIP_PARTNER
        override val titleRes: Int = R.string.type_relationship_partner
    }

    object RelationshipRelative : ContactDataType() {
        override val key: Key = RELATIONSHIP_RELATIVE
        override val titleRes: Int = R.string.type_relationship_relative
    }

    object RelationshipWork : ContactDataType() {
        override val key: Key = RELATIONSHIP_WORK
        override val titleRes: Int = R.string.type_relationship_work
    }

    object RelationshipFriend : ContactDataType() {
        override val key: Key = Key.RELATIONSHIP_FRIEND
        override val titleRes: Int = R.string.type_relationship_friend
    }

    enum class Key {
        PERSONAL,
        BUSINESS,
        MOBILE,
        MOBILE_BUSINESS,
        OTHER,
        BIRTHDAY,
        ANNIVERSARY,
        MAIN,
        CUSTOM,

        RELATIONSHIP_SIBLING,
        RELATIONSHIP_PARENT,
        RELATIONSHIP_CHILD,
        RELATIONSHIP_RELATIVE,
        RELATIONSHIP_PARTNER,
        RELATIONSHIP_FRIEND,
        RELATIONSHIP_WORK,
    }
}
