/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactimage

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage

fun ContactImage.toEntity(contactId: IContactIdInternal): ContactImageEntity =
    ContactImageEntity(contactId = contactId.uuid, thumbnailUri = thumbnailUri, fullImage = fullImage)

fun ContactImageEntity.toContactImage(): ContactImage =
    ContactImage(thumbnailUri = thumbnailUri, fullImage = fullImage, modelStatus = ModelStatus.UNCHANGED)
