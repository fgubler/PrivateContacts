/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.security.keystore.KeyProperties
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.DecryptionError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ifError
import ch.abwesend.privatecontacts.domain.model.result.generic.mapError
import ch.abwesend.privatecontacts.domain.model.result.generic.runCatchingAsResult
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.IKeyStoreRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionRepository : IEncryptionRepository {
    private val keyStoreRepository: IKeyStoreRepository by injectAnywhere()

    companion object {
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_SIZE_BITS = 256
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val GCM_IV_LENGTH_BYTES = 12

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 310_000
        private const val PBKDF2_SALT_LENGTH_BYTES = 16

        private const val JSON_VERSION = 1
    }

    // ---- File encryption (password-based AES-256-GCM with PBKDF2) ----

    override fun encrypt(plaintext: String, password: String): BinaryResult<String, Exception> = runCatchingAsResult {
        val salt = generateRandomBytes(PBKDF2_SALT_LENGTH_BYTES)
        val initializationVector = generateRandomBytes(GCM_IV_LENGTH_BYTES)
        val key = deriveKey(password, salt, PBKDF2_ITERATIONS, AES_KEY_SIZE_BITS)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, initializationVector))
        }
        val ciphertextBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val encoder = Base64.getEncoder()
        val payload = EncryptedPayload(
            version = JSON_VERSION,
            algorithm = AES_GCM_TRANSFORMATION,
            kdf = PBKDF2_ALGORITHM,
            iterations = PBKDF2_ITERATIONS,
            keySize = AES_KEY_SIZE_BITS,
            tagLength = GCM_TAG_LENGTH_BITS,
            salt = encoder.encodeToString(salt),
            iv = encoder.encodeToString(initializationVector),
            ciphertext = encoder.encodeToString(ciphertextBytes),
        )
        Json.encodeToString(payload)
    }.ifError { logger.error("Encryption failed", it) }

    override fun decrypt(ciphertext: String, password: String): BinaryResult<String, DecryptionError> = runCatchingAsResult {
        val decoder = Base64.getDecoder()
        val payload = Json.decodeFromString<EncryptedPayload>(ciphertext)
        val salt = decoder.decode(payload.salt)
        val iv = decoder.decode(payload.iv)
        val data = decoder.decode(payload.ciphertext)

        val key = deriveKey(password, salt, payload.iterations, payload.keySize)
        val cipher = Cipher.getInstance(payload.algorithm).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(payload.tagLength, iv))
        }
        cipher.doFinal(data).toString(Charsets.UTF_8)
    }.mapError { exception ->
        when (exception) {
            is AEADBadTagException -> {
                logger.error("Decryption failed due to invalid password", exception)
                DecryptionError.INVALID_PASSWORD
            }
            is SerializationException -> {
                logger.error("Not a valid JSON file", exception)
                DecryptionError.INVALID_FILE
            }
            is IllegalArgumentException -> {
                logger.error("Decryption failed due to invalid JSON structure", exception)
                DecryptionError.INVALID_FILE
            }
            else -> {
                logger.error("Decryption failed", exception)
                DecryptionError.UNKNOWN
            }
        }
    }

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int, keySize: Int): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keySize)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES)
    }

    // ---- Password storage (KeyStore-backed AES-256-GCM) ----

    override fun encryptPassword(password: String): BinaryResult<String, Exception> = runCatchingAsResult {
        val key = keyStoreRepository.getOrCreateKey()
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            .apply { init(Cipher.ENCRYPT_MODE, key) }
        val cipherText = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        val encoder = Base64.getEncoder()
        val payload = EncryptedPasswordPayload(
            version = JSON_VERSION,
            algorithm = AES_GCM_TRANSFORMATION,
            tagLength = GCM_TAG_LENGTH_BITS,
            iv = encoder.encodeToString(cipher.iv),
            ciphertext = encoder.encodeToString(cipherText),
        )
        Json.encodeToString(payload)
    }.ifError { logger.error("Password encryption failed", it) }

    override fun decryptPassword(encryptedPassword: String): BinaryResult<String, Exception> = runCatchingAsResult {
        val key = keyStoreRepository.getKey()
            ?: throw IllegalStateException("No KeyStore key available")

        val payload = Json.decodeFromString<EncryptedPasswordPayload>(encryptedPassword)
        val decoder = Base64.getDecoder()
        val iv = decoder.decode(payload.iv)
        val data = decoder.decode(payload.ciphertext)

        val cipher = Cipher.getInstance(payload.algorithm).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(payload.tagLength, iv))
        }
        cipher.doFinal(data).toString(Charsets.UTF_8)
    }.ifError { logger.error("Password decryption failed", it) }

    override fun deleteKeyStoreKey(): Boolean = keyStoreRepository.deleteKey()

    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}

@Serializable
private data class EncryptedPasswordPayload(
    val version: Int,
    val algorithm: String,
    val tagLength: Int,
    val iv: String,
    val ciphertext: String,
)

@Serializable
private data class EncryptedPayload(
    val version: Int,
    val algorithm: String,
    val kdf: String,
    val iterations: Int,
    val keySize: Int,
    val tagLength: Int,
    val salt: String,
    val iv: String,
    val ciphertext: String,
)
