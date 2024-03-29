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
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_BROTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_CHILD
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_FATHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_FRIEND
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_MOTHER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_PARENT
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_PARTNER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_RELATIVE
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_SIBLING
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_SISTER
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key.RELATIONSHIP_WORK
import ch.abwesend.privatecontacts.domain.util.StringProvider

sealed class ContactDataType {
    abstract val key: Key
    protected abstract val titleRes: Int
    protected open val isPlaceHolder: Boolean = false

    /**
     * Lower numbers mean more important.
     * Used if a piece of contact-data is stored under multiple types.
     */
    abstract val priority: Int

    open fun getTitle(stringProvider: StringProvider): String =
        stringProvider(titleRes)

    data object Mobile : ContactDataType() {
        override val key: Key = MOBILE
        override val titleRes: Int = R.string.type_mobile
        override val priority: Int = 100
    }

    data object Personal : ContactDataType() {
        override val key: Key = PERSONAL
        override val titleRes: Int = R.string.type_personal
        override val priority: Int = 200
    }

    data object Business : ContactDataType() {
        override val key: Key = BUSINESS
        override val titleRes: Int = R.string.type_business
        override val priority: Int = 300
    }

    data object MobileBusiness : ContactDataType() {
        override val key: Key = MOBILE_BUSINESS
        override val titleRes: Int = R.string.type_business
        override val priority: Int = 310
    }

    data object Birthday : ContactDataType() {
        override val key: Key = BIRTHDAY
        override val titleRes: Int = R.string.type_birthday
        override val priority: Int = 500
    }

    data object Anniversary : ContactDataType() {
        override val key: Key = ANNIVERSARY
        override val titleRes: Int = R.string.type_anniversary
        override val priority: Int = 600
    }

    data object Main : ContactDataType() {
        override val key: Key = MAIN
        override val titleRes: Int = R.string.type_main
        override val priority: Int = 700
    }

    /** The "Custom" which is shown in the dropdown of possible sub-types */
    data object Custom : ContactDataType() {
        override val key: Key = CUSTOM
        override val titleRes: Int = R.string.type_custom
        override val isPlaceHolder: Boolean = true
        override val priority: Int = 400
    }

    /** The "Custom" which is then created based on [customValue] */
    data class CustomValue(val customValue: String) : ContactDataType() {
        override val key: Key = CUSTOM
        override val titleRes: Int = R.string.type_custom
        override fun getTitle(stringProvider: StringProvider): String =
            customValue.ifEmpty { stringProvider(R.string.no_custom_label) }

        override val priority: Int = 410
    }

    data object RelationshipBrother : ContactDataType() {
        override val key: Key = RELATIONSHIP_BROTHER
        override val titleRes: Int = R.string.type_relationship_brother
        override val priority: Int = 800
    }

    data object RelationshipSister : ContactDataType() {
        override val key: Key = RELATIONSHIP_SISTER
        override val titleRes: Int = R.string.type_relationship_sister
        override val priority: Int = 802
    }

    data object RelationshipSibling : ContactDataType() {
        override val key: Key = RELATIONSHIP_SIBLING
        override val titleRes: Int = R.string.type_relationship_sibling
        override val priority: Int = 805
    }

    data object RelationshipParent : ContactDataType() {
        override val key: Key = RELATIONSHIP_PARENT
        override val titleRes: Int = R.string.type_relationship_parent
        override val priority: Int = 810
    }

    data object RelationshipFather : ContactDataType() {
        override val key: Key = RELATIONSHIP_FATHER
        override val titleRes: Int = R.string.type_relationship_father
        override val priority: Int = 811
    }

    data object RelationshipMother : ContactDataType() {
        override val key: Key = RELATIONSHIP_MOTHER
        override val titleRes: Int = R.string.type_relationship_mother
        override val priority: Int = 812
    }

    data object RelationshipChild : ContactDataType() {
        override val key: Key = RELATIONSHIP_CHILD
        override val titleRes: Int = R.string.type_relationship_child
        override val priority: Int = 820
    }

    data object RelationshipPartner : ContactDataType() {
        override val key: Key = RELATIONSHIP_PARTNER
        override val titleRes: Int = R.string.type_relationship_partner
        override val priority: Int = 830
    }

    data object RelationshipRelative : ContactDataType() {
        override val key: Key = RELATIONSHIP_RELATIVE
        override val titleRes: Int = R.string.type_relationship_relative
        override val priority: Int = 840
    }

    data object RelationshipFriend : ContactDataType() {
        override val key: Key = RELATIONSHIP_FRIEND
        override val titleRes: Int = R.string.type_relationship_friend
        override val priority: Int = 850
    }

    data object RelationshipWork : ContactDataType() {
        override val key: Key = RELATIONSHIP_WORK
        override val titleRes: Int = R.string.type_relationship_work
        override val priority: Int = 860
    }

    data object Other : ContactDataType() {
        override val key: Key = OTHER
        override val titleRes: Int = R.string.type_other
        override val priority: Int = 9000
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
        RELATIONSHIP_BROTHER,
        RELATIONSHIP_SISTER,
        RELATIONSHIP_PARENT,
        RELATIONSHIP_MOTHER,
        RELATIONSHIP_FATHER,
        RELATIONSHIP_CHILD,
        RELATIONSHIP_RELATIVE,
        RELATIONSHIP_PARTNER,
        RELATIONSHIP_FRIEND,
        RELATIONSHIP_WORK;

        companion object {
            fun parseOrNull(name: String): Key? = entries.firstOrNull { it.name == name.uppercase() }
        }
    }

    companion object {
        fun fromKey(key: Key, customValue: String?): ContactDataType = when (key) {
            PERSONAL -> Personal
            BUSINESS -> Business
            MOBILE -> Mobile
            MOBILE_BUSINESS -> MobileBusiness
            OTHER -> Other
            BIRTHDAY -> Birthday
            ANNIVERSARY -> Anniversary
            MAIN -> Main
            CUSTOM -> CustomValue(customValue.orEmpty())

            RELATIONSHIP_SIBLING -> RelationshipSibling
            RELATIONSHIP_BROTHER -> RelationshipBrother
            RELATIONSHIP_SISTER -> RelationshipSister
            RELATIONSHIP_PARENT -> RelationshipParent
            RELATIONSHIP_MOTHER -> RelationshipMother
            RELATIONSHIP_FATHER -> RelationshipFather
            RELATIONSHIP_CHILD -> RelationshipChild
            RELATIONSHIP_RELATIVE -> RelationshipRelative
            RELATIONSHIP_PARTNER -> RelationshipPartner
            RELATIONSHIP_FRIEND -> RelationshipFriend
            RELATIONSHIP_WORK -> RelationshipWork
        }
    }
}
