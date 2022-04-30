/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.repository.IDatabaseRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class DatabaseService {
    private val databaseRepository: IDatabaseRepository by injectAnywhere()

    suspend fun resetDatabase(): Boolean = databaseRepository.resetDatabase()
}
