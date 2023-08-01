package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export.toCategories
import ezvcard.VCard
import ezvcard.property.Kind
import ezvcard.property.Nickname
import ezvcard.property.Note
import ezvcard.property.StructuredName
import java.util.UUID

// TODO add unit tests & integration tests
class ContactToVCardMapper {
    fun mapToVCard(contact: IContact): BinaryResult<VCard, IContact> =
        try {
            val vCard = VCard()

            val uuid = when (val contactId = contact.id) {
                is IContactIdInternal -> contactId.uuid
                is IContactIdExternal -> UUID.randomUUID()
            }

            vCard.uid = uuid.toUid()

            val structuredName = StructuredName()
            structuredName.given = contact.firstName
            structuredName.family = contact.lastName
            vCard.structuredName = structuredName

            val nickname = Nickname()
            nickname.values.add(contact.nickname)
            vCard.addNickname(nickname)

            val note = Note(contact.notes)
            vCard.addNote(note)

            vCard.addContactData(contact)

            // TODO test with a dataset which actually has categories
            val categories = contact.contactGroups.toCategories()
            vCard.addCategories(categories)

            vCard.kind = Kind.individual() // TODO compute once non-person contacts are supported
            SuccessResult(vCard)
        } catch (e: Exception) {
            logger.warning("Failed to map contact '${contact.id}'")
            ErrorResult(contact)
        }

    private fun VCard.addContactData(contact: IContact) {
        // TODO implement
    }
}
