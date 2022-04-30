/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.repository.IDatabaseRepository

class DatabaseRepository : RepositoryBase(), IDatabaseRepository {
    override suspend fun resetDatabase(): Boolean = databaseHolder.resetDatabase()
}
