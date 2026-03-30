/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.IKeyStoreRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.module.Module
import org.koin.test.inject
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@ExperimentalCoroutinesApi
class EncryptionRepositoryTest : TestBase() {
    private val underTest: IEncryptionRepository by inject()

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.factory<IEncryptionRepository> { EncryptionRepository() }
        module.factory<IKeyStoreRepository> { FakeKeyStoreRepository() }
    }

    // ---- File encryption (password-based PBKDF2 + AES-256-GCM) ----

    @Test
    fun `encrypt and decrypt should round-trip correctly`() {
        val password = "s3cr3tP@ssw0rd"
        val plaintext = "Hello, World! This is a test contact backup."

        val ciphertext = underTest.encrypt(plaintext, password)
        val decrypted = underTest.decrypt(ciphertext, password)

        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt should return a JSON string with expected fields`() {
        val ciphertext = underTest.encrypt("some text", "password")

        assertThat(ciphertext).contains("\"algorithm\"")
        assertThat(ciphertext).contains("\"kdf\"")
        assertThat(ciphertext).contains("\"iterations\"")
        assertThat(ciphertext).contains("\"keySize\"")
        assertThat(ciphertext).contains("\"tagLength\"")
        assertThat(ciphertext).contains("\"salt\"")
        assertThat(ciphertext).contains("\"iv\"")
        assertThat(ciphertext).contains("\"ciphertext\"")
    }

    @Test
    fun `encrypt should produce different output each time (random salt and IV)`() {
        val password = "password123"
        val plaintext = "same plaintext"

        val ciphertext1 = underTest.encrypt(plaintext, password)
        val ciphertext2 = underTest.encrypt(plaintext, password)

        assertThat(ciphertext1).isNotEqualTo(ciphertext2)
    }

    @Test
    fun `decrypt should fail with wrong password`() {
        val plaintext = "sensitive data"
        val ciphertext = underTest.encrypt(plaintext, "correctPassword")

        assertThrows<Exception> {
            underTest.decrypt(ciphertext, "wrongPassword")
        }
    }

    @Test
    fun `encrypt and decrypt should handle empty plaintext`() {
        val password = "anyPassword"
        val plaintext = ""

        val ciphertext = underTest.encrypt(plaintext, password)
        val decrypted = underTest.decrypt(ciphertext, password)

        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt and decrypt should handle unicode content`() {
        val password = "unicodeTest"
        val plaintext = "Ünïcödé téxt with spëcïal chàracters 日本語"

        val ciphertext = underTest.encrypt(plaintext, password)
        val decrypted = underTest.decrypt(ciphertext, password)

        assertThat(decrypted).isEqualTo(plaintext)
    }

    // ---- Password storage (KeyStore-backed AES-256-GCM, mocked via FakeKeyStoreKeyProvider) ----

    @Test
    fun `encryptPassword and decryptPassword should round-trip correctly`() {
        val password = "mySecretBackupPassword"

        val encrypted = underTest.encryptPassword(password)
        val decrypted = underTest.decryptPassword(encrypted)

        assertThat(decrypted).isEqualTo(password)
    }

    @Test
    fun `encryptPassword should return a JSON string with expected fields`() {
        val encrypted = underTest.encryptPassword("somePassword")

        assertThat(encrypted).contains("\"algorithm\"")
        assertThat(encrypted).contains("\"tagLength\"")
        assertThat(encrypted).contains("\"iv\"")
        assertThat(encrypted).contains("\"ciphertext\"")
    }

    @Test
    fun `encryptPassword should produce different output each time (random IV)`() {
        val password = "samePassword"

        val encrypted1 = underTest.encryptPassword(password)
        val encrypted2 = underTest.encryptPassword(password)

        assertThat(encrypted1).isNotEqualTo(encrypted2)
    }

    @Test
    fun `decryptPassword should return null for invalid input`() {
        val decrypted = underTest.decryptPassword("not-valid-json-at-all")

        assertThat(decrypted).isNull()
    }

    @Test
    fun `decryptPassword should return null for tampered ciphertext`() {
        val encrypted = underTest.encryptPassword("originalPassword")
        val tampered = encrypted.dropLast(10) + "AAAAAAAAAA"

        val decrypted = underTest.decryptPassword(tampered)

        assertThat(decrypted).isNull()
    }
}

/** A pure JVM AES key provider — no Android KeyStore involved — for use in unit tests. */
private class FakeKeyStoreRepository : IKeyStoreRepository {
    private val secretKey: SecretKey by lazy {
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    }

    override fun getOrCreateKey(): SecretKey = secretKey
    override fun getKey(): SecretKey? = secretKey
    override fun deleteKey(): Boolean = true
}
