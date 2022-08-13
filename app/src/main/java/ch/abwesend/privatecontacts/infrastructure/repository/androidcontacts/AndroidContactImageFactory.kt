package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.thumbnailUri

fun Contact.getImage(): ContactImage =
    ContactImage(thumbnailUri = thumbnailUri.toString(), fullImage = imageData?.raw)
