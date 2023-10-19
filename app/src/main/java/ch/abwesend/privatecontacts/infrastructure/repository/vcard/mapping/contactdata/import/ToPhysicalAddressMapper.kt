/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.import

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ezvcard.property.Address

class ToPhysicalAddressMapper {
    private val addressService: IAddressFormattingService by injectAnywhere()

    fun toContactData(address: Address, sortOrder: Int): PhysicalAddress? = with(address) {
        val type = getContactDataType()
        val addressString = addressService.formatAddress(
            street = streetAddressFull.orEmpty(),
            neighborhood = "",
            postalCode = postalCode.orEmpty(),
            city = locality.orEmpty(),
            region = region.orEmpty(),
            country = country.orEmpty(),
        )

        addressString.takeIf { it.isNotEmpty() }?.let {
            PhysicalAddress.createEmpty(sortOrder)
                .changeType(type = type)
                .changeValue(value = it)
        }
    }

    private fun Address.getContactDataType(): ContactDataType =
        types.orEmpty().filterNotNull().map { it.toContactDataType() }.getByPriority()
}
