/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

interface IEncryptionRepository {
    /** Encrypts [plaintext] with AES-256-GCM using a key derived from [password]. */
    fun encrypt(plaintext: String, password: String): ByteArray

    /** Decrypts [ciphertext] previously produced by [encrypt]. */
    fun decrypt(ciphertext: ByteArray, password: String): String

    /**
     * Encrypts [password] with a key stored in the Android KeyStore and returns the
     * result as a Base64-encoded string suitable for storage in DataStore.
     */
    fun encryptPassword(password: String): String

    /**
     * Decrypts a password previously encrypted with [encryptPassword].
     * Returns null if decryption fails.
     */
    fun decryptPassword(encryptedPassword: String): String?

    /** Deletes the KeyStore key used to protect the backup password. */
    fun deleteKeyStoreKey()
}
