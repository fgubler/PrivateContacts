package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.thumbnailUri

fun Contact.getImage(): ContactImage {
    val thumbnailUriString = try {
        thumbnailUri.toString()
    } catch (e: Exception) {
        logger.warning("Failed to get thumbnailUri for contact $contactId")
        null
    }
    return ContactImage(thumbnailUri = thumbnailUriString, fullImage = imageData?.raw, modelStatus = UNCHANGED)
}
