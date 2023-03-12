package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import androidx.annotation.VisibleForTesting
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.BaseGenericContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdAndroid
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdAndroidWithoutNo
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.simpleClassName
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.LabeledValue

fun Contact.getContactData(
    telephoneService: TelephoneService,
    addressFormattingService: IAddressFormattingService,
): List<ContactData> {
    return getPhoneNumbers(telephoneService) +
        getEmailAddresses() +
        getPhysicalAddresses(addressFormattingService) +
        getWebsites() +
        getRelationships() +
        getEventDates() +
        getCompanies()
}

private fun Contact.getPhoneNumbers(telephoneService: TelephoneService): List<PhoneNumber> =
    phones.mapIndexed { index, phone ->
        val contactDataId = phone.toContactDataId()
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
    }.removePhoneNumberDuplicates()

private fun Contact.getEmailAddresses(): List<EmailAddress> {
    return mails.mapIndexed { index, email ->
        val contactDataId = email.toContactDataId()
        val type = email.label.toContactDataType()

        EmailAddress(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = email.value.raw,
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }.removeDuplicates()
}

private fun Contact.getPhysicalAddresses(formattingService: IAddressFormattingService): List<PhysicalAddress> =
    postalAddresses.mapIndexed { index, address ->
        val contactDataId = address.toContactDataId()
        val type = address.label.toContactDataType()

        val completeAddress = formattingService.formatAddress(
            street = address.value.street,
            neighborhood = address.value.neighborhood,
            city = address.value.city,
            postalCode = address.value.postCode,
            region = address.value.region,
            country = address.value.country,
        )

        PhysicalAddress(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = completeAddress,
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }.removeDuplicates()

private fun Contact.getWebsites(): List<Website> {
    return webAddresses.mapIndexed { index, address ->
        val contactDataId = address.toContactDataId()
        val type = address.label.toContactDataType()

        Website(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = address.value.raw.toString(),
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }.removeDuplicates()
}

private fun Contact.getRelationships(): List<Relationship> {
    return relations.mapIndexed { index, relationship ->
        val contactDataId = relationship.toContactDataId()
        val type = relationship.label.toContactDataType()

        Relationship(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = relationship.value.name,
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }.removeDuplicates()
}

private fun Contact.getEventDates(): List<EventDate> {
    return events.mapIndexed { index, event ->
        val contactDataId = event.toContactDataId()
        val type = event.label.toContactDataType()

        val date = with(event.value) {
            EventDate.createDate(day = dayOfMonth, month = month, year = year)
        }

        EventDate(
            id = contactDataId,
            sortOrder = index,
            type = type,
            value = date,
            isMain = index == 0,
            modelStatus = ModelStatus.UNCHANGED,
        )
    }.removeDuplicates()
}

private fun Contact.getCompanies(): List<Company> {
    val contactDataId = ContactDataIdInternal.randomId() // a bit of a hack but kind of unavoidable
    val type = ContactDataType.Business
    val index = 0 // there is only one company

    val company = Company(
        id = contactDataId,
        sortOrder = index,
        type = type,
        value = organization,
        isMain = index == 0,
        modelStatus = ModelStatus.UNCHANGED,
    ).takeIf { organization.isNotEmpty() }

    return listOfNotNull(company)
}

private fun LabeledValue<*>.toContactDataId(): IContactDataIdExternal =
    id?.let { contactDataNo -> ContactDataIdAndroid(contactDataNo = contactDataNo) }
        ?: ContactDataIdAndroidWithoutNo().also {
            logger.debug("No ID found for contact data of type ${label.simpleClassName}.")
        }

@VisibleForTesting
internal fun List<PhoneNumber>.removePhoneNumberDuplicates(): List<PhoneNumber> = removeDuplicates()
    .removeDuplicatesBy { it.formatValueForSearch() } // to remove duplicates with different formatting

@VisibleForTesting
internal fun <T : BaseGenericContactData<S>, S> List<T>.removeDuplicates(): List<T> =
    removeDuplicatesBy { it.displayValue }
        .removeDuplicatesBy { it.value }

private fun <T : BaseGenericContactData<S>, S, R> List<T>.removeDuplicatesBy(attributeSelector: (T) -> R): List<T> =
    groupBy(attributeSelector)
        .map { (_, value) -> value.minByOrNull { it.type.priority } }
        .filterNotNull()
