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

    companion object {
        val empty: ContactImage
            get() = ContactImage(thumbnailUri = null, fullImage = null, modelStatus = UNCHANGED)
    }
}
