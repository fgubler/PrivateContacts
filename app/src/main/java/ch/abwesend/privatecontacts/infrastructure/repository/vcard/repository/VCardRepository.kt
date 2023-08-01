/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ezvcard.Ezvcard
import ezvcard.VCard
import kotlinx.coroutines.withContext

class VCardRepository {
    val dispatchers: IDispatchers by injectAnywhere()

    /**
     * Beware: "Ezvcard.write(vCards)" for some reason loses or kicks out some properties
     *  - all relationships
     *  - anniversaries
     *
     * Interestingly, those properties are present in the VCard object but get lost during serialization
     */
    suspend fun exportVCards(vCards: List<VCard>): FileContent = withContext(dispatchers.io) {
        val content = Ezvcard.write(vCards).go()
        FileContent(content)
    }

    suspend fun importVCards(fileContent: FileContent): List<VCard> = withContext(dispatchers.io) {
        Ezvcard.parse(fileContent.content).all().orEmpty().filterNotNull().also {
            logger.debug("Loaded ${it.size} vcards from file")
        }
    }
}
