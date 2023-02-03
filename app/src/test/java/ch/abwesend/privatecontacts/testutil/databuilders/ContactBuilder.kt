/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil.databuilders

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.PUBLIC
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactEntity
import ch.abwesend.privatecontacts.testutil.TestContact

fun someContactId(): ContactIdInternal = someInternalContactId()
fun someInternalContactId(): ContactIdInternal = ContactIdInternal.randomId()
fun someExternalContactId(contactNo: Long = 442): ContactIdAndroid = ContactIdAndroid(contactNo = contactNo)

fun someContactBase(
    id: ContactId = someInternalContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "",
    type: ContactType = SECRET,
): IContactBase = ContactBase(
    id = id,
    type = type,
    displayName = getFullName(firstName, lastName, nickname)
)

fun someContactEntity(
    id: ContactIdInternal = someInternalContactId(),
    externalContactNo: Long? = null,
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    fullTextSearch: String = "TestSearch",
): ContactEntity = ContactEntity(
    rawId = id.uuid,
    externalContactNo = externalContactNo,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    fullTextSearch = fullTextSearch,
)

fun someContactEditable(
    id: ContactId = someInternalContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    image: ContactImage = ContactImage.empty,
    isNew: Boolean = false,
    saveInAccount: ContactAccount? = null,
): IContactEditable = someContactEditableGeneric(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactData = contactData,
    contactGroups = contactGroups,
    isNew = isNew,
    image = image,
    saveInAccount = saveInAccount,
)

fun someExternalContactEditable(
    id: IContactIdExternal = someExternalContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PUBLIC,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    image: ContactImage = ContactImage.empty,
    isNew: Boolean = false,
): IContactEditable = someContactEditableGeneric(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactData = contactData,
    contactGroups = contactGroups,
    isNew = isNew,
    image = image,
)

fun someContactEditableWithId(
    id: ContactIdInternal = someInternalContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    contactGroups: List<ContactGroup> = emptyList(),
    contactData: List<ContactData> = emptyList(),
    image: ContactImage = ContactImage.empty,
    isNew: Boolean = false,
): Pair<IContactIdInternal, IContactEditable> = id to someContactEditable(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    contactData = contactData,
    contactGroups = contactGroups,
    isNew = isNew,
    image = image,
)

fun someTestContact(
    id: ContactIdInternal = someInternalContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    image: ContactImage = ContactImage.empty,
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    isNew: Boolean = false,
): IContact = TestContact(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    image = image,
    contactDataSet = contactData,
    contactGroups = contactGroups,
    isNew = isNew,
)

fun <T : ContactId> someContactEditableGeneric(
    id: T,
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    type: ContactType = PUBLIC,
    notes: String = "Tries to do the right thing. Often badly.",
    image: ContactImage = ContactImage.empty,
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    isNew: Boolean = false,
    saveInAccount: ContactAccount? = null,
): IContactEditable = ContactEditable(
    id = id,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    type = type,
    notes = notes,
    image = image,
    contactDataSet = contactData.toMutableList(),
    contactGroups = contactGroups.toMutableList(),
    isNew = isNew,
    saveInAccount = saveInAccount,
)
