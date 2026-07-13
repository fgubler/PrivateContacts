/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import kotlinx.serialization.Serializable

/**
 * Serializable transport-representation of a secret contact used for synchronization.
 * Deliberately decoupled from the internal [ch.abwesend.privatecontacts.domain.model.contact.Contact]
 * and Room classes: only stable, cross-device data is carried (no Room row-ids, no android-ids).
 */
@Serializable
data class SyncContact(
    val syncId: String,
    val firstName: String,
    val lastName: String,
    val nickname: String,
    val middleName: String,
    val namePrefix: String,
    val nameSuffix: String,
    val notes: String,
    val contactData: List<SyncContactData>,
    val groups: List<String>,
    val image: SyncContactImage?,
)

@Serializable
data class SyncContactData(
    val category: ContactDataCategory,
    /** the [ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Key] name */
    val typeKey: String,
    /** only set for custom types */
    val typeCustomValue: String?,
    /** serialized value: raw string for string-based data, ISO-date for event-dates */
    val value: String,
    val sortOrder: Int,
    val isMain: Boolean,
)

@Serializable
data class SyncContactImage(
    val thumbnailUri: String?,
    /** base64-encoded full image */
    val fullImageBase64: String?,
)
