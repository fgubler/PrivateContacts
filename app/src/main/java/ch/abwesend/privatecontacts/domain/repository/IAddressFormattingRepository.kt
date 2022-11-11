package ch.abwesend.privatecontacts.domain.repository

interface IAddressFormattingRepository {
    fun formatAddress(
        street: String,
        neighborhood: String,
        postalCode: String,
        city: String,
        region: String,
        country: String,
        useFallbackForEmptyAddress: Boolean = true,
    ): String
}
