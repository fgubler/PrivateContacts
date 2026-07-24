/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.importexport.DecryptionError
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult

interface IEncryptionRepository {
    /**
     * Encrypts [plaintext] with AES-256-GCM using a key derived from [password] via PBKDF2.
     * Returns a JSON string containing all parameters (algorithm, iterations, salt, IV, ciphertext)
     * needed for decryption, with binary values Base64-encoded.
     */
    fun encrypt(plaintext: String, password: String): BinaryResult<String, Exception>

    /** Decrypts a JSON string previously produced by [encrypt]. */
    fun decrypt(ciphertext: String, password: String): BinaryResult<String, DecryptionError>

    /**
     * Encrypts [password] with the KeyStore key for [purpose] and returns the
     * result as a Base64-encoded string suitable for storage in DataStore.
     */
    fun encryptPassword(password: String, purpose: KeyStorePurpose): BinaryResult<String, Exception>

    /**
     * Decrypts a password previously encrypted with [encryptPassword] for the same [purpose].
     * Returns an [ErrorResult] if decryption fails.
     */
    fun decryptPassword(encryptedPassword: String, purpose: KeyStorePurpose): BinaryResult<String, Exception>

    /**
     * Deletes the KeyStore key protecting the password/passphrase of [purpose].
     * @return true if the key was successfully deleted.
     */
    fun deleteKeyStoreKey(purpose: KeyStorePurpose): Boolean
}
