package ch.abwesend.privatecontacts.testutil.databuilders

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportPartialData.CreatedVCards
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.toUid
import ezvcard.VCard
import ezvcard.property.Organization
import ezvcard.property.StructuredName
import io.mockk.mockk
import java.util.UUID

fun someUri(): Uri = mockk()

fun someFileContent(
    content: String = "This is not a proper vcard content but let us try"
): FileContent = FileContent(content)

fun someVCard(
    uid: UUID = UUID.randomUUID(),
    firstName: String = "Dante",
    lastName: String = "Alighieri",
    organizationNames: List<String> = emptyList(),
): VCard {
    val vCard = VCard()

    vCard.uid = uid.toUid()
    vCard.structuredName = StructuredName()
    vCard.structuredName.given = firstName
    vCard.structuredName.family = lastName

    val organizations = organizationNames.map { name -> someOrganization(organizationName = name) }.toMutableList()
    vCard.organizations.addAll(organizations)

    return vCard
}

fun someOrganization(organizationName: String): Organization {
    val organization = Organization()
    organization.values.add(organizationName)
    return organization
}

fun someStructuredOrganization(organizationNames: List<String>): Organization {
    val organization = Organization()
    organization.values.addAll(organizationNames)
    return organization
}

fun someParsedData(
    successfulContacts: List<IContactEditable> = emptyList(),
    numberOfErrors: Int = 0,
): ContactImportPartialData.ParsedData =
    ContactImportPartialData.ParsedData(successfulContacts, numberOfErrors)

fun someCreatedVCards(
    fileContent: FileContent = someFileContent(),
    failedContacts: List<IContactEditable> = emptyList(),
): CreatedVCards = CreatedVCards(fileContent, failedContacts)
