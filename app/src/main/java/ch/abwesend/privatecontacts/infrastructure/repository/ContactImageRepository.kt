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
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase

class ContactImageRepository : RepositoryBase() {
    suspend fun loadImage(contactId: IContactIdInternal): ContactImage = withDatabase { database ->
        logger.debug("Loading image for contact $contactId")
        val entity = database.contactImageDao().getImage(contactId.uuid)
        entity?.toContactImage() ?: ContactImage.empty.also {
            logger.debug("No image found for contact $contactId")
        }
    }

    /** @return true if the image was changed in the database */
    suspend fun storeImage(
        contactId: IContactIdInternal,
        contactImage: ContactImage
    ): Boolean = withDatabase { database ->
        if (contactImage.unchanged) {
            logger.debug("The image for contact $contactId was not changed")
            false
        } else {
            logger.debug("Deleting image for contact $contactId")
            database.contactImageDao().deleteImage(contactId.uuid)
            database.storeImage(contactId, contactImage)
            true
        }
    }

    private suspend fun AppDatabase.storeImage(contactId: IContactIdInternal, contactImage: ContactImage) {
        if (contactImage.isEmpty) {
            logger.debug("No image to store for contact $contactId")
        } else {
            logger.debug("Storing new image for contact $contactId")
            val entity = contactImage.toEntity(contactId)
            contactImageDao().insert(entity)
        }
    }
}
