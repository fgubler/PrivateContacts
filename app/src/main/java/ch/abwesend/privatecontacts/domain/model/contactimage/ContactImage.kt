/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactimage

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED

data class ContactImage(
    val thumbnailUri: String?,
    val fullImage: ByteArray?,
    val modelStatus: ModelStatus,
) {
    val isEmpty: Boolean
        get() = thumbnailUri.isNullOrEmpty() && (fullImage == null || fullImage.isEmpty())

    fun contentEquals(other: ContactImage): Boolean {
        return thumbnailUri == other.thumbnailUri &&
            if (fullImage != null && other.fullImage != null) fullImage.isEqualTo(other.fullImage)
            else fullImage == null && other.fullImage == null
    }

    companion object {
        val empty: ContactImage
            get() = ContactImage(thumbnailUri = null, fullImage = null, modelStatus = UNCHANGED)
    }
}

private fun ByteArray.isEqualTo(other: ByteArray): Boolean =
    size == other.size && (0..size).all { this[it] == other[it] }
