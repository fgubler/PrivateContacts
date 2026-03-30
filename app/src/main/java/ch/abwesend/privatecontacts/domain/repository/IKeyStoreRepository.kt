/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import javax.crypto.SecretKey

interface IKeyStoreRepository {
    fun getOrCreateKey(): SecretKey
    fun getKey(): SecretKey?

    /** @return true if the deletion was successful */
    fun deleteKey(): Boolean
}
