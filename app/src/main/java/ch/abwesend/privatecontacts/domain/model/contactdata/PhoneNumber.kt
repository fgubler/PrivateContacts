package ch.abwesend.privatecontacts.domain.model.contactdata

data class PhoneNumber(
    val value: String,
    val type: ContactDataSubType,
    val isMainNumber: Boolean = false,
)
