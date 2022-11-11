package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactDataDiff
import ch.abwesend.privatecontacts.domain.model.contact.ContactDiff
import ch.abwesend.privatecontacts.domain.model.contact.ContactGroupDiff
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup

// TODO use from repository
class ContactDiffService {
    fun computeContactDiff(oldContact: IContact, newContact: IContact): ContactDiff {
        val oldContactDataById = oldContact.contactDataSet.associateBy { it.id }
        val newContactDataById = newContact.contactDataSet.associateBy { it.id }
        val contactDataIds = oldContactDataById.keys + newContactDataById.keys
        val contactDataDiff = contactDataIds.mapNotNull { contactDataId ->
            val oldContactData = oldContactDataById[contactDataId]
            val newContactData = newContactDataById[contactDataId]
            computeContactDataDiff(oldContactData, newContactData)
        }

        val oldContactGroupsById = oldContact.contactGroups.associateBy { it.id }
        val newContactGroupsById = newContact.contactGroups.associateBy { it.id }
        val contactGroupIds = oldContactGroupsById.keys + newContactGroupsById.keys
        val contactGroupDiff = contactGroupIds.mapNotNull { groupId ->
            val oldContactGroup = oldContactGroupsById[groupId]
            val newContactGroup = newContactGroupsById[groupId]
            computeContactGroupDiff(oldContactGroup, newContactGroup)
        }

        return ContactDiff(
            firstName = newContact.firstName.takeIf { oldContact.firstName != newContact.firstName },
            lastName = newContact.lastName.takeIf { oldContact.lastName != newContact.lastName },
            nickname = newContact.nickname.takeIf { oldContact.nickname != newContact.nickname },
            notes = newContact.notes.takeIf { oldContact.notes != newContact.notes },
            image = newContact.image.takeUnless { oldContact.image.contentEquals(newContact.image) },
            contactDataSet = contactDataDiff,
            contactGroups = contactGroupDiff,
        )
    }

    private fun computeContactDataDiff(oldContactData: ContactData?, newContactData: ContactData?): ContactDataDiff<*>? {
        return when {
            oldContactData == null && newContactData == null -> null
            oldContactData != null && newContactData == null -> ContactDataDiff(
                id = oldContactData.id,
                value = oldContactData.value,
                deleted = true,
            )
            newContactData != null -> ContactDataDiff(
                id = newContactData.id,
                value = newContactData.value,
                deleted = false,
            )
            else -> throw IllegalStateException("This cannot happen: all logical branches are covered")
        }
    }

    private fun computeContactGroupDiff(
        oldContactGroup: ContactGroup?,
        newContactGroup: ContactGroup?
    ): ContactGroupDiff? {
        return when {
            oldContactGroup == null && newContactGroup == null -> null
            oldContactGroup != null && newContactGroup == null -> ContactGroupDiff(
                id = oldContactGroup.id,
                notes = oldContactGroup.notes,
                deleted = true,
            )
            newContactGroup != null -> ContactGroupDiff(
                id = newContactGroup.id,
                notes = newContactGroup.notes,
                deleted = false,
            )
            else -> throw IllegalStateException("This cannot happen: all logical branches are covered")
        }
    }
}
