package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdInternal
import java.util.UUID

val ContactDataId.uuid: UUID
    get() = (this as IContactDataIdInternal).uuid
