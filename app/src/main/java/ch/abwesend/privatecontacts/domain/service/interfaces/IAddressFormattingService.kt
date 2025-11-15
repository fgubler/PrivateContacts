package ch.abwesend.privatecontacts.domain.service.interfaces

interface IAddressFormattingService {
    fun formatAddress(
        street: String,
        neighborhood: String,
        postalCode: String,
        city: String,
        region: String,
        country: String,
    ): String
}
