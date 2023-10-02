/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion.V3
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion.V4
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ezvcard.Ezvcard
import ezvcard.VCard
import kotlinx.coroutines.withContext
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion as CustomVCardVersion
import ezvcard.VCardVersion as EzVCardVersion

class VCardRepository {
    val dispatchers: IDispatchers by injectAnywhere()

    /** Beware: "Ezvcard.write(vCards)" in [V3] loses some data [CustomVCardVersion] for details */
    suspend fun exportVCards(vCards: List<VCard>, version: CustomVCardVersion): FileContent = withContext(dispatchers.io) {
        val vCardVersion = version.toVCardVersion()
        val content = Ezvcard.write(vCards).version(vCardVersion).go()
        FileContent(content)
    }

    /** Accepts VCF files in both [V3] and [V4] */
    suspend fun importVCards(fileContent: FileContent): List<VCard> = withContext(dispatchers.io) {
        Ezvcard.parse(fileContent.content).all().orEmpty().filterNotNull().also {
            logger.debug("Loaded ${it.size} vcards from file")
        }
    }

    private fun CustomVCardVersion.toVCardVersion() =
        when (this) {
            V3 -> EzVCardVersion.V3_0
            V4 -> EzVCardVersion.V4_0
        }
}
