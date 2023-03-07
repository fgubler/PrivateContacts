package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import com.alexstyl.contactstore.MutableContactGroup
import com.alexstyl.contactstore.ContactGroup as AndroidContactGroup

fun List<AndroidContactGroup>.toContactGroups(): List<ContactGroup> =
    map { it.toContactGroup() }
        .distinctBy { it.id.name } // needs to be unique within the app

fun AndroidContactGroup.toContactGroup(): ContactGroup {
    val id = ContactGroupId(name = title, groupNo = groupId)
    return ContactGroup(id = id, notes = note.orEmpty(), modelStatus = UNCHANGED)
}

fun ContactGroup.toNewAndroidContactGroup(account: ContactAccount): MutableContactGroup {
    val androidGroup = MutableContactGroup()
    androidGroup.title = id.name
    androidGroup.note = notes
    androidGroup.account = account.toInternetAccountOrNull()
    return androidGroup
}
