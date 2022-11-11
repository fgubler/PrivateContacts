package ch.abwesend.privatecontacts.domain.model.contactdata

data class PhysicalAddressDto(
    val street: String,
    val neighborhood: String,
    val postalCode: String,
    val city: String,
    val region: String,
    val country: String,
)
