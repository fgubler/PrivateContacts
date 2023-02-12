package ch.abwesend.privatecontacts.domain.model.contactgroup

sealed interface IContactGroupId {
    val name: String
    /** for contact-groups from Android */
    val groupNo: Long?
}

data class ContactGroupId(
    override val name: String,
    override val groupNo: Long?
) : IContactGroupId
