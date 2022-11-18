package ch.abwesend.privatecontacts.domain.model.contactgroup

import ch.abwesend.privatecontacts.domain.model.ModelStatus

interface IContactGroup {
    val id: IContactGroupId
    val notes: String
    val modelStatus: ModelStatus
}

data class ContactGroup(
    override val id: IContactGroupId,
    override val notes: String,
    override val modelStatus: ModelStatus,
) : IContactGroup
