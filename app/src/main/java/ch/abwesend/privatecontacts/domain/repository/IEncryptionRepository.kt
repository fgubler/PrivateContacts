/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

interface IEncryptionRepository {
    /**
     * Encrypts [plaintext] with AES-256-GCM using a key derived from [password] via PBKDF2.
     * Returns a JSON string containing all parameters (algorithm, iterations, salt, IV, ciphertext)
     * needed for decryption, with binary values Base64-encoded.
     */
    fun encrypt(plaintext: String, password: String): BinaryResult<String, Exception>

    /** Decrypts a JSON string previously produced by [encrypt]. */
    fun decrypt(ciphertext: String, password: String): String

    /**
     * Encrypts [password] with a key stored in the Android KeyStore and returns the
     * result as a Base64-encoded string suitable for storage in DataStore.
     */
    fun encryptPassword(password: String): BinaryResult<String, Exception>

    /**
     * Decrypts a password previously encrypted with [encryptPassword].
     * Returns an [ErrorResult] if decryption fails.
     */
    fun decryptPassword(encryptedPassword: String): BinaryResult<String, Exception>

    /**
     * Deletes the KeyStore key used to protect the backup password.
     * @return true if the key was successfully deleted.
     */
    fun deleteKeyStoreKey(): Boolean
}
