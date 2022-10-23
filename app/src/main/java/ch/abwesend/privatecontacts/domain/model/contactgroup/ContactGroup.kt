package ch.abwesend.privatecontacts.domain.model.contactgroup

interface IContactGroup {
    val id: IContactGroupId
    val notes: String
}

data class ContactGroup(
    override val id: IContactGroupId,
    override val notes: String,
) : IContactGroup
