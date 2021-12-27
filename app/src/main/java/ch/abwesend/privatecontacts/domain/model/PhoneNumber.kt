package ch.abwesend.privatecontacts.domain.model

data class PhoneNumber(
    val value: String,
    val type: PhoneNumberType,
    val isMainNumber: Boolean = false,
)
