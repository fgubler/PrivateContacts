/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import javax.crypto.SecretKey

/**
 * Identifies which independent KeyStore key a crypto operation should use. Backup and contact-sync
 * each get their own key so disabling/resetting one feature can never make the other's stored
 * password/passphrase undecryptable.
 */
enum class KeyStorePurpose {
    BACKUP,
    SYNC,
}

interface IKeyStoreRepository {
    fun getOrCreateKey(purpose: KeyStorePurpose): SecretKey
    fun getKey(purpose: KeyStorePurpose): SecretKey?

    /** @return true if the deletion was successful */
    fun deleteKey(purpose: KeyStorePurpose): Boolean
}
