package ch.abwesend.privatecontacts.testutil.databuilders

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.contact.IContactEditable
import ch.abwesend.privatecontacts.domain.model.importexport.ContactImportPartialData
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ezvcard.VCard
import ezvcard.property.StructuredName
import io.mockk.mockk

fun someUri(): Uri = mockk()

fun someFileContent(
    content: String = "This is not a proper vcard content but let us try"
): FileContent = FileContent(content)

fun someVCard(
    firstName: String = "Dante",
    lastName: String = "Alighieri",
): VCard {
    val vCard = VCard()

    vCard.structuredName = StructuredName()
    vCard.structuredName.given = firstName
    vCard.structuredName.family = lastName

    return vCard
}

fun someParsedData(
    successfulContacts: List<IContactEditable> = emptyList(),
    numberOfErrors: Int = 0,
): ContactImportPartialData.ParsedData =
    ContactImportPartialData.ParsedData(successfulContacts, numberOfErrors)
