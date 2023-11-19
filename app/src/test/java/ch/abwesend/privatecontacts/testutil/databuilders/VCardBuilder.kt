package ch.abwesend.privatecontacts.testutil.databuilders

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType.Main
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportPartialData.CreatedVCards
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.toUid
import ch.abwesend.privatecontacts.infrastructure.service.CUSTOM_RELATIONSHIP_TYPE_ORGANISATION
import ezvcard.VCard
import ezvcard.parameter.RelatedType
import ezvcard.property.Address
import ezvcard.property.Anniversary
import ezvcard.property.Birthday
import ezvcard.property.Email
import ezvcard.property.Organization
import ezvcard.property.Related
import ezvcard.property.StructuredName
import ezvcard.property.Telephone
import ezvcard.property.Url
import io.mockk.mockk
import java.time.LocalDate
import java.util.UUID

fun someUri(): Uri = mockk()

fun someFileContent(
    content: String = "This is not a proper vcard content but let us try"
): FileContent = FileContent(content)

fun someVCard(
    uid: UUID = UUID.randomUUID(),
    firstName: String = "Dante",
    lastName: String = "Alighieri",
    phoneNumbers: List<String> = emptyList(),
    emailAddresses: List<String> = emptyList(),
    streetAddresses: List<String> = emptyList(),
    siblings: List<String> = emptyList(),
    websites: List<String> = emptyList(),
    anniversaries: List<LocalDate> = emptyList(),
    birthdays: List<LocalDate> = emptyList(),
    organizationNames: List<String> = emptyList(),
): VCard {
    val vCard = VCard()

    vCard.uid = uid.toUid()
    vCard.structuredName = StructuredName()
    vCard.structuredName.given = firstName
    vCard.structuredName.family = lastName

    val organizations = organizationNames.map { name -> someVCardOrganization(organizationName = name) }
    vCard.organizations.addAll(organizations)

    val vCardPhoneNumbers = phoneNumbers.map { number -> someVCardPhoneNumber(number) }
    vCard.telephoneNumbers.addAll(vCardPhoneNumbers)

    val vCardEmails = emailAddresses.map { email -> someVCardEmail(email) }
    vCard.emails.addAll(vCardEmails)

    val vCardAddresses = streetAddresses.map { street -> someVCardAddress(street = street) }
    vCard.addresses.addAll(vCardAddresses)

    val vCardRelations = siblings.map { sibling -> someVCardRelation(sibling) }
    vCard.relations.addAll(vCardRelations)

    val vCardUrls = websites.map { website -> someVCardUrl(website) }
    vCard.urls.addAll(vCardUrls)

    val vCardBirthdays = birthdays.map { birthday -> someVCardBirthday(birthday) }
    vCard.birthdays.addAll(vCardBirthdays)

    val vCardAnniversaries = anniversaries.map { anniversary -> someVCardAnniversary(anniversary) }
    vCard.anniversaries.addAll(vCardAnniversaries)

    return vCard
}

fun someVCardOrganization(organizationName: String?): Organization {
    val organization = Organization()
    organization.values.add(organizationName)
    return organization
}

fun someStructuredOrganization(organizationNames: List<String?>): Organization {
    val organization = Organization()
    organization.values.addAll(organizationNames)
    return organization
}

fun someVCardPhoneNumber(number: String? = "123456"): Telephone = Telephone(number)

fun someVCardEmail(email: String? = "alpha@beta.ch"): Email = Email(email)

fun someVCardAddress(
    street: String? = "Some Street",
    locality: String? = "San Francisco",
    postalCode: String? = "8888",
    region: String? = "California",
    country: String? = "USA",
): Address = Address().also {
    it.streetAddress = street
    it.locality = locality
    it.postalCode = postalCode
    it.region = region
    it.country = country
}

fun someVCardRelation(relation: String? = "Some Relation"): Related = Related(relation).also {
    it.text = relation
}

fun someCompanyVCardPseudoRelation(companyName: String? = "Ergon", type: ContactDataType = Main): Related =
    Related(companyName).also {
        it.text = companyName
        it.types.add(RelatedType.get("${CUSTOM_RELATIONSHIP_TYPE_ORGANISATION}${type.key.name}"))
    }

fun someVCardUrl(url: String? = "https://www.google.com"): Url = Url(url)

fun someVCardAnniversary(date: LocalDate?): Anniversary = Anniversary(date)

fun someVCardBirthday(date: LocalDate?): Birthday = Birthday(date)

fun someParsedData(
    successfulContacts: List<IContactEditable> = emptyList(),
    numberOfErrors: Int = 0,
): ContactImportPartialData.ParsedData =
    ContactImportPartialData.ParsedData(successfulContacts, numberOfErrors)

fun someCreatedVCards(
    fileContent: FileContent = someFileContent(),
    failedContacts: List<IContactEditable> = emptyList(),
): CreatedVCards = CreatedVCards(fileContent, failedContacts)
