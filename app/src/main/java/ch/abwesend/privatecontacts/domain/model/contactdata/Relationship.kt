/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Custom
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Other
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipBrother
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipChild
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipFather
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipFriend
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipMother
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipPartner
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipRelative
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipSister
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.RelationshipWork

data class Relationship(
    override val id: ContactDataId,
    override val sortOrder: Int,
    override val value: String,
    override val type: ContactDataType,
    override val isMain: Boolean = false,
    override val modelStatus: ModelStatus,
) : StringBasedContactDataGeneric<Relationship> {
    override val category: ContactDataCategory = ContactDataCategory.RELATIONSHIP

    override val allowedTypes: List<ContactDataType>
        get() = defaultAllowedTypes

    override fun changeValue(value: String): Relationship {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(value = value, modelStatus = status)
    }

    override fun changeType(type: ContactDataType): Relationship {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(type = type, modelStatus = status)
    }

    override fun overrideStatus(newStatus: ModelStatus) = copy(modelStatus = newStatus)
    override fun changeToInternalId(): ContactData = copy(id = createContactDataId())
    override fun changeToExternalId(): ContactData = copy(id = createExternalDummyContactDataId())
    override fun changeSortOrder(newSortOrder: Int): Relationship {
        val status = modelStatus.tryChangeTo(CHANGED)
        return copy(sortOrder = newSortOrder, modelStatus = status)
    }

    override fun delete(): Relationship {
        val status = modelStatus.tryChangeTo(ModelStatus.DELETED)
        return copy(modelStatus = status)
    }

    companion object {
        val icon = Icons.Default.Group
        const val labelSingular = R.string.relationship
        const val labelPlural = R.string.relationships

        private val defaultAllowedTypes = listOf(
            RelationshipFather,
            RelationshipMother,
            RelationshipChild,
            RelationshipBrother,
            RelationshipSister,
            RelationshipFriend,
            RelationshipPartner,
            RelationshipRelative,
            RelationshipWork,
            Custom,
            Other,
        )

        fun createEmpty(sortOrder: Int): Relationship =
            Relationship(
                id = createContactDataId(),
                sortOrder = sortOrder,
                type = RelationshipFriend,
                value = "",
                isMain = (sortOrder == 0),
                modelStatus = ModelStatus.NEW,
            )
    }
}
