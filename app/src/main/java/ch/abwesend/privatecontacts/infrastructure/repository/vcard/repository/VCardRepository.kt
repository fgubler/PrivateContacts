/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.util.Constants
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ezvcard.Ezvcard
import ezvcard.VCard
import kotlinx.coroutines.withContext

// TODO consider using fallbacks with different formats
class VCardRepository {
    val dispatchers: IDispatchers by injectAnywhere()

    suspend fun exportVCards(vCards: List<VCard>): FileContent = withContext(dispatchers.io) {
        val content = Ezvcard.write(vCards).go()
        val lines = content.split(Constants.linebreak)
        FileContent(lines)
    }

    suspend fun importVCards(fileContent: FileContent): List<VCard> = withContext(dispatchers.io) {
        val content = fileContent.joinToString(separator = Constants.linebreak)
        Ezvcard.parse(content).all().orEmpty().filterNotNull().also {
            logger.debug("Loaded ${it.size} vcards from file")
        }
    }
}
