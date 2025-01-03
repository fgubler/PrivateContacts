/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactimage

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.WithModelStatus

data class ContactImage(
    val thumbnailUri: String?,
    val fullImage: ByteArray?,
    override val modelStatus: ModelStatus,
) : WithModelStatus {
    val isEmpty: Boolean
        get() = thumbnailUri.isNullOrEmpty() && (fullImage == null || fullImage.isEmpty())

    fun contentEquals(other: ContactImage): Boolean {
        return thumbnailUri == other.thumbnailUri &&
            if (fullImage != null && other.fullImage != null) fullImage.isEqualTo(other.fullImage)
            else fullImage == null && other.fullImage == null
    }

    fun change(fullImage: ByteArray?): ContactImage {
        val newModelStatus = if (fullImage == null) {
            if (isEmpty) UNCHANGED else ModelStatus.DELETED
        } else {
            if (isEmpty) ModelStatus.NEW else ModelStatus.CHANGED
        }
        return copy(fullImage = fullImage, modelStatus = newModelStatus)
    }

    fun deleteFullImage() = change(fullImage = null)

    companion object {
        val empty: ContactImage
            get() = ContactImage(thumbnailUri = null, fullImage = null, modelStatus = UNCHANGED)
    }
}

private fun ByteArray.isEqualTo(other: ByteArray): Boolean =
    size == other.size && (0..size).all { this[it] == other[it] }
