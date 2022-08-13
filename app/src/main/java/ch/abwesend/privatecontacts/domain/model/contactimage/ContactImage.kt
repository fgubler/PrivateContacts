/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactimage

class ContactImage(
    val thumbnailUri: String?,
    val fullImage: ByteArray?,
    val unchanged: Boolean,
) {
    val isEmpty: Boolean
        get() = thumbnailUri.isNullOrEmpty() && (fullImage == null || fullImage.isEmpty())

    companion object {
        val empty: ContactImage
            get() = ContactImage(thumbnailUri = null, fullImage = null, unchanged = true)
    }
}
