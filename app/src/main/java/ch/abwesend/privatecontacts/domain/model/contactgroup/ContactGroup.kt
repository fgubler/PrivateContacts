package ch.abwesend.privatecontacts.domain.model.contactgroup

sealed interface IContactGroup {
    val id: IContactGroupId
}

data class ContactGroup(
    override val id: IContactGroupId
) : IContactGroup
