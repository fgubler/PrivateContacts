/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EncryptionRepositoryTest {

    private val underTest = EncryptionRepository()

    @Test
    fun `encrypt and decrypt should round-trip correctly`() {
        val password = "s3cr3tP@ssw0rd"
        val plaintext = "Hello, World! This is a test contact backup.".toByteArray(Charsets.UTF_8)

        val ciphertext = underTest.encrypt(plaintext, password)
        val decrypted = underTest.decrypt(ciphertext, password)

        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt should produce different output each time (random salt and IV)`() {
        val password = "password123"
        val plaintext = "same plaintext".toByteArray(Charsets.UTF_8)

        val ciphertext1 = underTest.encrypt(plaintext, password)
        val ciphertext2 = underTest.encrypt(plaintext, password)

        assertThat(ciphertext1).isNotEqualTo(ciphertext2)
    }

    @Test
    fun `decrypt should fail with wrong password`() {
        val plaintext = "sensitive data".toByteArray(Charsets.UTF_8)
        val ciphertext = underTest.encrypt(plaintext, "correctPassword")

        assertThrows<Exception> {
            underTest.decrypt(ciphertext, "wrongPassword")
        }
    }

    @Test
    fun `encrypt and decrypt should handle empty plaintext`() {
        val password = "anyPassword"
        val plaintext = ByteArray(0)

        val ciphertext = underTest.encrypt(plaintext, password)
        val decrypted = underTest.decrypt(ciphertext, password)

        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `encrypt and decrypt should handle unicode content`() {
        val password = "unicodeTest"
        val original = "Ünïcödé téxt with spëcïal chàracters 日本語"
        val plaintext = original.toByteArray(Charsets.UTF_8)

        val ciphertext = underTest.encrypt(plaintext, password)
        val decrypted = underTest.decrypt(ciphertext, password)

        assertThat(decrypted.toString(Charsets.UTF_8)).isEqualTo(original)
    }
}
