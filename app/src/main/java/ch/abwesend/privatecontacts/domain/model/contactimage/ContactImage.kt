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

    fun change(fullImage: ByteArray?): ContactImage = copy(fullImage = fullImage, modelStatus = ModelStatus.CHANGED)

    companion object {
        val empty: ContactImage
            get() = ContactImage(thumbnailUri = null, fullImage = null, modelStatus = UNCHANGED)
    }
}

private fun ByteArray.isEqualTo(other: ByteArray): Boolean =
    size == other.size && (0..size).all { this[it] == other[it] }
