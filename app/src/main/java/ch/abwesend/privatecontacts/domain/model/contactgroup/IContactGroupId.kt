package ch.abwesend.privatecontacts.domain.model.contactgroup

sealed interface IContactGroupId {
    val name: String
}

@JvmInline
value class ContactGroupId(
    override val name: String
) : IContactGroupId
