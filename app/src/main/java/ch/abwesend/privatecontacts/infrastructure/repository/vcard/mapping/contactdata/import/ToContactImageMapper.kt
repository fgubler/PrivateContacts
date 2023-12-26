/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ezvcard.property.Photo

fun List<Photo>.toContactImage(): ContactImage {
    val fullImages = mapNotNull { it.data }
    val thumbnailUris = mapNotNull { it.url }

    return ContactImage(
        thumbnailUri = thumbnailUris.firstOrNull(),
        fullImage = fullImages.firstOrNull(),
        modelStatus = ModelStatus.NEW,
    )
}
