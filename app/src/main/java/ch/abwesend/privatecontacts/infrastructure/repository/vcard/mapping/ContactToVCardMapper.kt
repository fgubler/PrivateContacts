package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ezvcard.VCard
import java.util.UUID

// TODO add unit tests
class ContactToVCardMapper {
    fun mapToVCard(contact: IContact): VCard {
        val vCard = VCard()

        val uuid = when (val contactId = contact.id) {
            is IContactIdInternal -> contactId.uuid
            is IContactIdExternal -> UUID.randomUUID()
        }

        vCard.uid = uuid.toUid()

        // TODO implement
        return vCard
    }
}
