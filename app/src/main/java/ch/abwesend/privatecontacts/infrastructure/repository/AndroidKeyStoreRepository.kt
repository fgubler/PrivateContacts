/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.repository.IKeyStoreRepository
import ch.abwesend.privatecontacts.domain.repository.KeyStorePurpose
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeyStoreRepository : IKeyStoreRepository {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val BACKUP_KEY_ALIAS = "PrivateContactsBackupKey"
        private const val SYNC_KEY_ALIAS = "PrivateContactsSyncKey"
        private const val AES_KEY_SIZE_BITS = 256
    }

    private fun aliasFor(purpose: KeyStorePurpose): String = when (purpose) {
        KeyStorePurpose.BACKUP -> BACKUP_KEY_ALIAS
        KeyStorePurpose.SYNC -> SYNC_KEY_ALIAS
    }

    override fun getOrCreateKey(purpose: KeyStorePurpose): SecretKey {
        val alias = aliasFor(purpose)
        val existing = withKeyStore { it.getKey(alias, null) as? SecretKey }
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(AES_KEY_SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    override fun getKey(purpose: KeyStorePurpose): SecretKey? = withKeyStore { keyStore ->
        keyStore.getKey(aliasFor(purpose), null) as? SecretKey
    }

    override fun deleteKey(purpose: KeyStorePurpose): Boolean {
        val alias = aliasFor(purpose)
        return try {
            withKeyStore { keyStore ->
                if (keyStore.containsAlias(alias)) {
                    keyStore.deleteEntry(alias)
                    logger.debug("Deleted KeyStore key for $purpose")
                }
            }
            true
        } catch (e: Exception) {
            logger.warning("Failed to delete KeyStore key for $purpose", e)
            false
        }
    }

    private fun <T> withKeyStore(block: (KeyStore) -> T): T {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
        return block(keyStore)
    }
}
