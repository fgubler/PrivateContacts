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
import ch.abwesend.privatecontacts.domain.model.contact.ContactImportId
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
import java.util.UUID

fun someContactId(): ContactIdInternal = someInternalContactId()
fun someInternalContactId(): ContactIdInternal = ContactIdInternal.randomId()
fun someContactImportId(): ContactImportId = ContactImportId(UUID.randomUUID())

fun someExternalContactId(contactNo: Long = 442, lookupKey: String = "Test"): ContactIdAndroid =
    ContactIdAndroid(contactNo = contactNo, lookupKey = lookupKey)

fun someImportId(uuid: UUID = UUID.randomUUID()): ContactImportId = ContactImportId(uuid)

fun someContactBase(
    id: ContactId = someInternalContactId(),
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "",
    middleName: String = "",
    namePrefix: String = "",
    nameSuffix: String = "",
    type: ContactType = SECRET,
): IContactBase = ContactBase(
    id = id,
    type = type,
    displayName = getFullName(firstName, lastName, nickname, middleName, namePrefix, nameSuffix)
)

fun someContactEntity(
    id: ContactIdInternal = someInternalContactId(),
    externalContactNo: Long? = null,
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    middleName: String = "Middle",
    namePrefix: String = "pre",
    nameSuffix: String = "suf",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    fullTextSearch: String = "TestSearch",
): ContactEntity = ContactEntity(
    rawId = id.uuid,
    importId = null,
    externalContactNo = externalContactNo,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    middleName = middleName,
    namePrefix = namePrefix,
    nameSuffix = nameSuffix,
    type = type,
    notes = notes,
    fullTextSearch = fullTextSearch,
)

/** decides on internal- vs. external-Contact by the type of the passed ID */
fun someContactEditableByIdType(
    id: ContactId,
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    image: ContactImage = ContactImage.empty,
    isNew: Boolean = false,
    saveInAccount: ContactAccount = ContactAccount.None,
): IContactEditable =
    when (id) {
        is IContactIdExternal -> someExternalContactEditable(
            id = id,
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            notes = notes,
            contactData = contactData,
            contactGroups = contactGroups,
            isNew = isNew,
            image = image,
            saveInAccount = saveInAccount,
        )
        is IContactIdInternal -> someContactEditable(
            id = id,
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            notes = notes,
            contactData = contactData,
            contactGroups = contactGroups,
            isNew = isNew,
            image = image,
            saveInAccount = saveInAccount,
        )
    }

fun someContactEditable(
    id: ContactId = someInternalContactId(),
    importId: ContactImportId? = null,
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    middleName: String = "",
    namePrefix: String = "",
    nameSuffix: String = "",
    type: ContactType = SECRET,
    notes: String = "Tries to do the right thing. Often badly.",
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    image: ContactImage = ContactImage.empty,
    isNew: Boolean = false,
    saveInAccount: ContactAccount = ContactAccount.None,
): IContactEditable = someContactEditableGeneric(
    id = id,
    importId = importId,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    middleName = middleName,
    namePrefix = namePrefix,
    nameSuffix = nameSuffix,
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
    saveInAccount: ContactAccount = ContactAccount.LocalPhoneContacts,
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
    middleName = "",
    namePrefix = "",
    nameSuffix = "",
    type = type,
    notes = notes,
    image = image,
    contactDataSet = contactData,
    contactGroups = contactGroups,
    isNew = isNew,
)

fun <T : ContactId> someContactEditableGeneric(
    id: T,
    importId: ContactImportId? = null,
    firstName: String = "John",
    lastName: String = "Snow",
    nickname: String = "Lord Snow",
    middleName: String = "Middle",
    namePrefix: String = "pre",
    nameSuffix: String = "suf",
    type: ContactType = PUBLIC,
    notes: String = "Tries to do the right thing. Often badly.",
    image: ContactImage = ContactImage.empty,
    contactData: List<ContactData> = emptyList(),
    contactGroups: List<ContactGroup> = emptyList(),
    isNew: Boolean = false,
    saveInAccount: ContactAccount = ContactAccount.None,
): IContactEditable = ContactEditable(
    id = id,
    importId = importId,
    firstName = firstName,
    lastName = lastName,
    nickname = nickname,
    middleName = middleName,
    namePrefix = namePrefix,
    nameSuffix = nameSuffix,
    type = type,
    notes = notes,
    image = image,
    contactDataSet = contactData.toMutableList(),
    contactGroups = contactGroups.toMutableList(),
    isNew = isNew,
    saveInAccount = saveInAccount,
)

fun someOnlineAccount(
    username: String = "yoda@jedi.com",
    provider: String = "jedi.com",
): ContactAccount.OnlineAccount = ContactAccount.OnlineAccount(username = username, accountProvider = provider)
