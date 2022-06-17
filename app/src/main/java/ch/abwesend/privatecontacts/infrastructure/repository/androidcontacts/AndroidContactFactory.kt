package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import com.alexstyl.contactstore.Contact

fun Contact.toContactBase(): IContactBase? =
    try {
        ContactBase(
            id = ContactIdAndroid(contactNo = contactId),
            type = ContactType.PUBLIC,
            displayName = displayName,
        )
    } catch (t: Throwable) {
        logger.warning("Failed to map android contact with id = $contactId", t)
        null
    }

fun Contact.toContact(): IContact? =
    try {
        ContactEditable(
            id = ContactIdAndroid(contactNo = contactId),
            type = ContactType.PUBLIC,
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            notes = note?.raw.orEmpty(),
            contactDataSet = getContactData().toMutableList(),
        )
    } catch (t: Throwable) {
        logger.warning("Failed to map android contact with id = $contactId", t)
        null
    }
