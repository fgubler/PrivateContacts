package ch.abwesend.privatecontacts.domain.model.contactgroup

sealed interface IContactGroupId {
    val name: String

    /** for contact-groups from Android */
    val groupNo: Long?

    fun changeName(newName: String): IContactGroupId
}

// TODO consider how to make sure that Equality is handled properly (the name alone should be enough in most cases)
data class ContactGroupId(
    override val name: String,
    override val groupNo: Long?
) : IContactGroupId {
    override fun changeName(newName: String) = copy(name = newName)
}
