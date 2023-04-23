package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactCategory.ORGANIZATION
import ch.abwesend.privatecontacts.domain.model.contact.ContactCategory.PERSON
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactGroup

class AndroidContactMapper {
    private val contactDataFactory: AndroidContactDataMapper by injectAnywhere()

    fun toContactBase(contact: Contact, rethrowExceptions: Boolean): IContactBase? =
        try {
            ContactBase(
                id = ContactIdAndroid(contactNo = contact.contactId),
                type = ContactType.PUBLIC,
                displayName = contact.displayName,
            )
        } catch (t: Throwable) {
            logger.warning("Failed to map android contact with id = ${contact.contactId}", t)
            if (rethrowExceptions) throw t
            else null
        }

    fun toContact(
        contact: Contact,
        groups: List<ContactGroup>,
        rethrowExceptions: Boolean
    ): IContact? = with(contact) {
        try {
            val middleNamePart = if (middleName.isBlank()) "" else " $middleName"
            val category = if (organization.isEmpty()) PERSON else ORGANIZATION
            ContactEditable(
                id = ContactIdAndroid(contactNo = contactId),
                type = ContactType.PUBLIC,
                category = category,
                firstName = "$firstName$middleNamePart",
                lastName = lastName,
                nickname = nickname,
                organizationName = organization,
                notes = note?.raw.orEmpty(),
                image = getImage(),
                contactDataSet = contactDataFactory.getContactData(contact).toMutableList(),
                contactGroups = groups.toContactGroups().toMutableList(),
                saveInAccount = ContactAccount.None,
            )
        } catch (t: Throwable) {
            logger.warning("Failed to map android contact with id = $contactId", t)
            if (rethrowExceptions) throw t
            else null
        }
    }
}
