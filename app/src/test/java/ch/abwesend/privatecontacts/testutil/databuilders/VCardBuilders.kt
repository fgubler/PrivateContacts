package ch.abwesend.privatecontacts.testutil.databuilders

import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ezvcard.VCard
import ezvcard.property.StructuredName

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
