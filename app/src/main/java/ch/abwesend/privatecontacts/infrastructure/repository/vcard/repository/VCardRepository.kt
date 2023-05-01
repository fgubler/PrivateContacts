/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ezvcard.Ezvcard
import ezvcard.VCard
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.Path

// TODO add tests
// TODO consider using fallbacks with different formats
class VCardRepository {
    val dispatchers: IDispatchers by injectAnywhere()

    suspend fun exportVCards(vCards: List<VCard>, targetFile: File) = withContext(dispatchers.io) {
        val path = Path(targetFile.absolutePath)
        Ezvcard.write(vCards).go(path)
    }

    suspend fun importVCards(sourceFile: File): List<VCard> = withContext(dispatchers.io) {
        val path = Path(sourceFile.absolutePath)
        Ezvcard.parse(path).all().orEmpty().filterNotNull().also {
            logger.debug("Loaded ${it.size} vcards from file")
        }
    }
}
