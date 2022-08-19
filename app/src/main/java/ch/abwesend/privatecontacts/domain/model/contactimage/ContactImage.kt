/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactimage

data class ContactImage(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactImage

        if (thumbnailUri != other.thumbnailUri) return false
        if (fullImage != null) {
            if (other.fullImage == null) return false
            if (!fullImage.contentEquals(other.fullImage)) return false
        } else if (other.fullImage != null) return false
        if (unchanged != other.unchanged) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbnailUri?.hashCode() ?: 0
        result = 31 * result + (fullImage?.contentHashCode() ?: 0)
        result = 31 * result + unchanged.hashCode()
        return result
    }
}
