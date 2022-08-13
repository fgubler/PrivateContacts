/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactimage

class ContactImage(
    val thumbnailUri: String?,
    val fullImage: ByteArray?,
) {
    companion object {
        val empty: ContactImage
            get() = ContactImage(thumbnailUri = null, fullImage = null)
    }
}
