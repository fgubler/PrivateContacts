package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contact.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contact.IContactDataIdInternal
import java.util.UUID

val ContactDataId.uuid: UUID
    get() = (this as IContactDataIdInternal).uuid
