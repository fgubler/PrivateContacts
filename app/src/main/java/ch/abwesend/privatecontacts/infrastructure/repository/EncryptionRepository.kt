/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionRepository : IEncryptionRepository {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEYSTORE_KEY_ALIAS = "PrivateContactsBackupKey"

        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_SIZE_BITS = 256
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val GCM_IV_LENGTH_BYTES = 12

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 310_000
        private const val PBKDF2_SALT_LENGTH_BYTES = 16
    }

    // ---- File encryption (password-based AES-256-GCM with PBKDF2) ----

    override fun encrypt(plaintext: ByteArray, password: String): ByteArray {
        val salt = generateRandomBytes(PBKDF2_SALT_LENGTH_BYTES)
        val initializationVector = generateRandomBytes(GCM_IV_LENGTH_BYTES)
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, initializationVector))
        }
        val ciphertext = cipher.doFinal(plaintext)

        // Layout: [salt (16)] [iv (12)] [ciphertext + GCM tag]
        return salt + initializationVector + ciphertext
    }

    override fun decrypt(ciphertext: ByteArray, password: String): ByteArray {
        val salt = ciphertext.copyOfRange(0, PBKDF2_SALT_LENGTH_BYTES)
        val iv = ciphertext.copyOfRange(PBKDF2_SALT_LENGTH_BYTES, PBKDF2_SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES)
        val data = ciphertext.copyOfRange(PBKDF2_SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES, ciphertext.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }
        return cipher.doFinal(data)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES)
    }

    // ---- Password storage (KeyStore-backed AES-256-GCM) ----

    override fun encryptPassword(password: String): String {
        val key = getOrCreateKeyStoreKey()
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val initializationVector = cipher.iv
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        // Layout: [iv (12)] [ciphertext + GCM tag]
        val combined = initializationVector + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    override fun decryptPassword(encryptedPassword: String): String? = try {
        val combined = Base64.getDecoder().decode(encryptedPassword)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val data = combined.copyOfRange(GCM_IV_LENGTH_BYTES, combined.size)

        val key = getKeyStoreKey() ?: return null
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        cipher.doFinal(data).toString(Charsets.UTF_8)
    } catch (e: Exception) {
        logger.warning("Failed to decrypt backup password", e)
        null
    }

    override fun deleteKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
            if (keyStore.containsAlias(KEYSTORE_KEY_ALIAS)) {
                keyStore.deleteEntry(KEYSTORE_KEY_ALIAS)
                logger.debug("Deleted KeyStore key for backup encryption")
            }
        } catch (e: Exception) {
            logger.warning("Failed to delete KeyStore key", e)
        }
    }

    private fun getOrCreateKeyStoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
        keyStore.getKey(KEYSTORE_KEY_ALIAS, null)?.let { return it as SecretKey }

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

    private fun getKeyStoreKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
        return keyStore.getKey(KEYSTORE_KEY_ALIAS, null) as? SecretKey
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}
