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
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeyStoreRepository : IKeyStoreRepository {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEYSTORE_KEY_ALIAS = "PrivateContactsBackupKey"
        private const val AES_KEY_SIZE_BITS = 256
    }

    override fun getOrCreateKey(): SecretKey {
        val existing = withKeyStore { it.getKey(KEYSTORE_KEY_ALIAS, null) as? SecretKey }
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(AES_KEY_SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    override fun getKey(): SecretKey? = withKeyStore { keyStore ->
        keyStore.getKey(KEYSTORE_KEY_ALIAS, null) as? SecretKey
    }

    override fun deleteKey() {
        try {
            withKeyStore { keyStore ->
                if (keyStore.containsAlias(KEYSTORE_KEY_ALIAS)) {
                    keyStore.deleteEntry(KEYSTORE_KEY_ALIAS)
                    logger.debug("Deleted KeyStore key for backup encryption")
                }
            }
        } catch (e: Exception) {
            logger.warning("Failed to delete KeyStore key", e)
        }
    }

    private fun <T> withKeyStore(block: (KeyStore) -> T): T {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
        return block(keyStore)
    }
}
