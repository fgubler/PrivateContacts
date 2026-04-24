/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.IKeyStoreRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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

        val encryptionResult = underTest.encrypt(plaintext, password)
        assertThat(encryptionResult).isInstanceOf(SuccessResult::class.java)
        val ciphertext = (encryptionResult as SuccessResult).value

        val decryptionResult = underTest.decrypt(ciphertext, password)
        assertThat(decryptionResult).isInstanceOf(SuccessResult::class.java)
        val decrypted = (decryptionResult as SuccessResult).value

        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt should return a JSON string with expected fields`() {
        val result = underTest.encrypt("some text", "password")
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val ciphertext = (result as SuccessResult).value

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

        val result1 = underTest.encrypt(plaintext, password)
        val result2 = underTest.encrypt(plaintext, password)

        assertThat(result1).isInstanceOf(SuccessResult::class.java)
        assertThat(result2).isInstanceOf(SuccessResult::class.java)
        val ciphertext1 = (result1 as SuccessResult).value
        val ciphertext2 = (result2 as SuccessResult).value

        assertThat(ciphertext1).isNotEqualTo(ciphertext2)
    }

    @Test
    fun `decrypt should fail with wrong password`() {
        val plaintext = "sensitive data"
        val result = underTest.encrypt(plaintext, "correctPassword")
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val ciphertext = (result as SuccessResult).value

        val decryptResult = underTest.decrypt(ciphertext, "wrongPassword")
        assertThat(decryptResult).isInstanceOf(ErrorResult::class.java)
    }

    @Test
    fun `encrypt and decrypt should handle empty plaintext`() {
        val password = "anyPassword"
        val plaintext = ""

        val encryptionResult = underTest.encrypt(plaintext, password)
        assertThat(encryptionResult).isInstanceOf(SuccessResult::class.java)
        val ciphertext = (encryptionResult as SuccessResult).value

        val decryptionResult = underTest.decrypt(ciphertext, password)
        assertThat(decryptionResult).isInstanceOf(SuccessResult::class.java)
        val decrypted = (decryptionResult as SuccessResult).value

        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt and decrypt should handle unicode content`() {
        val password = "unicodeTest"
        val plaintext = "Ünïcödé téxt with spëcïal chàracters 日本語"

        val result = underTest.encrypt(plaintext, password)
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val ciphertext = (result as SuccessResult).value

        val decryptResult = underTest.decrypt(ciphertext, password)
        assertThat(decryptResult).isInstanceOf(SuccessResult::class.java)
        val decrypted = (decryptResult as SuccessResult).value

        assertThat(decrypted).isEqualTo(plaintext)
    }

    // ---- Password storage (KeyStore-backed AES-256-GCM, mocked via FakeKeyStoreKeyProvider) ----

    @Test
    fun `encryptPassword and decryptPassword should round-trip correctly`() {
        val password = "mySecretBackupPassword"

        val result = underTest.encryptPassword(password)
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val encrypted = (result as SuccessResult).value
        val decryptResult = underTest.decryptPassword(encrypted)
        assertThat(decryptResult).isInstanceOf(SuccessResult::class.java)
        val decrypted = (decryptResult as SuccessResult).value

        assertThat(decrypted).isEqualTo(password)
    }

    @Test
    fun `encryptPassword should return a JSON string with expected fields`() {
        val result = underTest.encryptPassword("somePassword")
        assertThat(result).isInstanceOf(SuccessResult::class.java)
        val encrypted = (result as SuccessResult).value

        assertThat(encrypted).contains("\"algorithm\"")
        assertThat(encrypted).contains("\"tagLength\"")
        assertThat(encrypted).contains("\"iv\"")
        assertThat(encrypted).contains("\"ciphertext\"")
    }

    @Test
    fun `encryptPassword should produce different output each time (random IV)`() {
        val password = "samePassword"

        val result1 = underTest.encryptPassword(password)
        val result2 = underTest.encryptPassword(password)

        assertThat(result1).isInstanceOf(SuccessResult::class.java)
        assertThat(result2).isInstanceOf(SuccessResult::class.java)
        val encrypted1 = (result1 as SuccessResult).value
        val encrypted2 = (result2 as SuccessResult).value

        assertThat(encrypted1).isNotEqualTo(encrypted2)
    }

    @Test
    fun `decryptPassword should return ErrorResult for invalid input`() {
        val result = underTest.decryptPassword("not-valid-json-at-all")

        assertThat(result).isInstanceOf(ErrorResult::class.java)
    }

    @Test
    fun `decryptPassword should return ErrorResult for tampered ciphertext`() {
        val encrypted = (underTest.encryptPassword("originalPassword") as SuccessResult).value
        val tampered = encrypted.dropLast(10) + "AAAAAAAAAA"

        val result = underTest.decryptPassword(tampered)

        assertThat(result).isInstanceOf(ErrorResult::class.java)
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
