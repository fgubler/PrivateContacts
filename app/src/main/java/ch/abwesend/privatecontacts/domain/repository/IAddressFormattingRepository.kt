package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddressDto

interface IAddressFormattingRepository {
    fun formatAddress(address: PhysicalAddressDto): String
}
