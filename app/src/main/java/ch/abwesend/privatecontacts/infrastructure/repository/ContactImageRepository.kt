/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.toContactImage
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.toEntity

class ContactImageRepository : RepositoryBase() {
    suspend fun storeImage(contactId: IContactIdInternal, contactImage: ContactImage) = withDatabase { database ->
        logger.debug("Replacing image for contact $contactId")
        val entity = contactImage.toEntity(contactId)
        database.contactImageDao().deleteImage(contactId.uuid)
        database.contactImageDao().insert(entity)
    }

    suspend fun loadImage(contactId: IContactIdInternal): ContactImage = withDatabase { database ->
        logger.debug("Loading image for contact $contactId")
        val entity = database.contactImageDao().getImage(contactId.uuid)
        entity.toContactImage()
    }
}
