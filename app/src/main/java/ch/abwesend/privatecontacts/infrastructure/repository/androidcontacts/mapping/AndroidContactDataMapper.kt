package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping

import androidx.annotation.VisibleForTesting
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.BaseGenericContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataIdAndroid
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.IContactDataIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.domain.model.contactdata.createExternalDummyContactDataId
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.domain.util.simpleClassName
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactCompanyMappingService
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.Label
import com.alexstyl.contactstore.LabeledValue
import com.alexstyl.contactstore.Relation

class AndroidContactDataMapper {
    private val telephoneService: TelephoneService by injectAnywhere()
    private val addressFormattingService: IAddressFormattingService by injectAnywhere()
    private val companyMappingService: AndroidContactCompanyMappingService by injectAnywhere()

    fun getContactData(contact: Contact): List<ContactData> = with(contact) {
        getPhoneNumbers() +
            getEmailAddresses() +
            getPhysicalAddresses() +
            getWebsites() +
            getRelationships() +
            getEventDates() +
            getCompanies()
    }

    private fun Contact.getPhoneNumbers(): List<PhoneNumber> =
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
        }.let { removePhoneNumberDuplicates(it) }

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

    private fun Contact.getPhysicalAddresses(): List<PhysicalAddress> =
        postalAddresses.mapIndexed { index, address ->
            val contactDataId = address.toContactDataId()
            val type = address.label.toContactDataType()

            val completeAddress = addressFormattingService.formatAddress(
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
        return relations
            .filterNot { relation -> isPseudoRelationForCompany(relation) }
            .mapIndexed { index, relationship ->
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
        return relations
            .filter { relation -> isPseudoRelationForCompany(relation) }
            .mapIndexed { index, company ->
                val contactDataId = company.toContactDataId()
                val label = company.label as? Label.Custom
                val type = label?.let { companyMappingService.decodeFromPseudoRelationshipLabel(it.label) }
                    ?: ContactDataType.Business

                Company(
                    id = contactDataId,
                    sortOrder = index,
                    type = type,
                    value = company.value.name,
                    isMain = index == 0,
                    modelStatus = ModelStatus.UNCHANGED,
                )
            }
    }

    private fun LabeledValue<*>.toContactDataId(): IContactDataIdExternal =
        id?.let { contactDataNo -> ContactDataIdAndroid(contactDataNo = contactDataNo) }
            ?: createExternalDummyContactDataId().also {
                logger.debug("No ID found for contact data of type ${label.simpleClassName}.")
            }

    @VisibleForTesting
    internal fun removePhoneNumberDuplicates(phoneNumbers: List<PhoneNumber>): List<PhoneNumber> =
        phoneNumbers.removeDuplicates()
            .removeDuplicatesBy { it.formatValueForSearch() } // to remove duplicates with different formatting

    @VisibleForTesting
    internal fun isPseudoRelationForCompany(relation: LabeledValue<Relation>): Boolean {
        val label = relation.label
        return label is Label.Custom && companyMappingService.matchesCompanyCustomRelationshipPattern(label.label)
    }

    private fun <T : BaseGenericContactData<S>, S> List<T>.removeDuplicates(): List<T> =
        removeDuplicatesBy { it.displayValue }
            .removeDuplicatesBy { it.value }

    private fun <T : BaseGenericContactData<S>, S, R> List<T>.removeDuplicatesBy(attributeSelector: (T) -> R): List<T> =
        groupBy(attributeSelector)
            .map { (_, value) -> value.minByOrNull { it.type.priority } }
            .filterNotNull()
}
