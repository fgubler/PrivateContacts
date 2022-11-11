package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroupId
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage

/**
 * Represents the changes between two contacts.
 *  - a value being null means that nothing there was changed.
 *  - a value being non-null represents the new value.
 */
data class ContactDiff(
    val firstName: String?,
    val lastName: String?,
    val nickname: String?,
    val notes: String?,
    val image: ContactImage?,
    val contactDataSet: List<ContactDataDiff<*>>,
    val contactGroups: List<ContactGroupDiff>,
)

data class ContactDataDiff<T>(
    val id: ContactDataId,
    val value: T?,
    val deleted: Boolean,
)

data class ContactGroupDiff(
    val id: IContactGroupId,
    val notes: String?,
    val deleted: Boolean,
)
