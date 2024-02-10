/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.toContactImage
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.toEntity

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
        val entity = contactImage.toEntity(contactId)
        when (contactImage.modelStatus) {
            ModelStatus.CHANGED -> {
                logger.debug("Updating image for contact $contactId")
                database.contactImageDao().update(entity)
                true
            }
            ModelStatus.DELETED -> {
                logger.debug("Deleting image for contact $contactId")
                database.contactImageDao().delete(entity)
                true
            }
            ModelStatus.NEW -> {
                logger.debug("Creating image for contact $contactId")
                if (contactImage.isEmpty) false else {
                    database.contactImageDao().insert(entity)
                    true
                }
            }
            ModelStatus.UNCHANGED -> { false }
        }
    }
}
