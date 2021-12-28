package ch.abwesend.privatecontacts.domain.model

interface ContactBase {
    val id: Int
    val firstName: String
    val lastName: String
    val nickname: String
}

interface Contact : ContactBase {
    val phoneNumbers: List<PhoneNumber>
    val notes: String
}

data class ContactLite(
    override val id: Int,
    override val firstName: String,
    override val lastName: String,
    override val nickname: String,
) : ContactBase

data class ContactFull(
    override val id: Int,
    override var firstName: String,
    override var lastName: String,
    override var nickname: String,
    override var notes: String,
    override var phoneNumbers: List<PhoneNumber>,
) : Contact

fun ContactBase.getFullName(firstNameFirst: Boolean) =
    if (firstNameFirst) "$firstName $lastName"
    else "$lastName $firstName"
