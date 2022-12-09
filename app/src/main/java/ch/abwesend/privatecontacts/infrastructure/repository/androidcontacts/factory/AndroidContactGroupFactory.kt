package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import com.alexstyl.contactstore.ContactGroup as AndroidContactGroup

fun List<AndroidContactGroup>.toContactGroups(): List<ContactGroup> =
    map { it.toContactGroup() }

fun AndroidContactGroup.toContactGroup(): ContactGroup =
    ContactGroup(id = ContactGroupId(name = title), notes = note.orEmpty(), modelStatus = UNCHANGED)
