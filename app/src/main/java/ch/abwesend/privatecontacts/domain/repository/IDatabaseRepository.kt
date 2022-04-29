/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

interface IDatabaseRepository {
    /**
     * @return true if the reset was successful
     */
    suspend fun resetDatabase(): Boolean
}
