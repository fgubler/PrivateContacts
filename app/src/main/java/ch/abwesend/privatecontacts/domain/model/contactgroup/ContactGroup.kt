package ch.abwesend.privatecontacts.domain.model.contactgroup

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.WithModelStatus

interface IContactGroup : WithModelStatus {
    val id: IContactGroupId
    val notes: String
}

data class ContactGroup(
    override val id: IContactGroupId,
    override val notes: String,
    override val modelStatus: ModelStatus,
) : IContactGroup
