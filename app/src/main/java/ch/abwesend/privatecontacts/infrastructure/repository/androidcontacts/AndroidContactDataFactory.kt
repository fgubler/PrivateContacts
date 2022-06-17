package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactDataIdAndroid
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.Label

// TODO add additional data-types
fun Contact.getContactData(): List<ContactData> = getPhoneNumbers() +
    getEmailAddresses() +
    getPhysicalAddresses() +
    getWebsites()

private fun Contact.getPhoneNumbers(): List<PhoneNumber> {
    val telephoneService: TelephoneService by injectAnywhere()

    return phones.mapIndexed { index, phone ->
        // TODO think about the ID fallback?
        val contactDataId = ContactDataIdAndroid(contactDataNo = phone.id ?: 0L)
        val type = phone.label.toContactDataType()
        val number = phone.value.raw

        PhoneNumber(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = number,
            formattedValue = telephoneService.formatPhoneNumberForDisplay(number),
            valueForMatching = telephoneService.formatPhoneNumberForMatching(number),
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }
}

private fun Contact.getEmailAddresses(): List<EmailAddress> {
    return mails.mapIndexed { index, email ->
        // TODO think about the ID fallback?
        val contactDataId = ContactDataIdAndroid(contactDataNo = email.id ?: 0L)
        val type = email.label.toContactDataType()

        EmailAddress(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = email.value.raw,
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }
}

private fun Contact.getPhysicalAddresses(): List<PhysicalAddress> {
    return postalAddresses.mapIndexed { index, address ->
        // TODO think about the ID fallback?
        val contactDataId = ContactDataIdAndroid(contactDataNo = address.id ?: 0L)
        val type = address.label.toContactDataType()

        // TODO think about how to structure the address
        val completeAddress = listOfNotNull(
            address.value.street,
            address.value.neighborhood,
            address.value.postCode,
            address.value.city,
            address.value.region,
            address.value.country
        ).joinToString(separator = ",\n")

        PhysicalAddress(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = completeAddress,
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }
}

private fun Contact.getWebsites(): List<Website> {
    return webAddresses.mapIndexed { index, address ->
        // TODO think about the ID fallback?
        val contactDataId = ContactDataIdAndroid(contactDataNo = address.id ?: 0L)
        val type = address.label.toContactDataType()

        Website(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = address.value.raw.toString(),
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }
}

// TODO find a solution for not losing the information about the other types
private fun Label.toContactDataType(): ContactDataType =
    when (this) {
        Label.PhoneNumberMobile -> ContactDataType.Mobile
        Label.PhoneNumberCompanyMain -> ContactDataType.Business
        Label.PhoneNumberWorkMobile -> ContactDataType.MobileBusiness
        Label.Main -> ContactDataType.Main
        Label.LocationHome -> ContactDataType.Personal
        Label.LocationWork -> ContactDataType.Business
        Label.DateBirthday -> ContactDataType.Birthday
        Label.DateAnniversary -> ContactDataType.Anniversary
        Label.WebsiteHomePage -> ContactDataType.Main
        Label.Other -> ContactDataType.Other
        is Label.Custom -> ContactDataType.CustomValue(customValue = label)
        else -> ContactDataType.Other
    }
