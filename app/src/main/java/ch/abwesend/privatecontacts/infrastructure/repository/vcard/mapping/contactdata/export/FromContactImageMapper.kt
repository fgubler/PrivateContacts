/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ezvcard.property.Photo

fun ContactImage.toPhotos(): List<Photo> = listOfNotNull(
    fullImage?.let { image -> Photo(image, ImageTypeDetector.detectImageType(image)) },
    thumbnailUri?.let { uri -> Photo(uri, null) }
)
