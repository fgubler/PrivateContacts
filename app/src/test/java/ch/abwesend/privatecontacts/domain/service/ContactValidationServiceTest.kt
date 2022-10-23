/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Failure
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationResult.Success
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactValidationServiceTest : TestBase() {
    private lateinit var underTest: ContactValidationService

    override fun setup() {
        super.setup()
        underTest = ContactValidationService()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "  "])
    fun `should not allow first- and last-name and nickname all to be empty or blank`(name: String) {
        val contact = someContactEditable(firstName = name, lastName = name, nickname = name)

        val result = runBlocking { underTest.validateContact(contact) }

        assertThat(result).isInstanceOf(Failure::class.java)
        assertThat((result as Failure).validationErrors).containsExactly(NAME_NOT_SET)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "  "])
    fun `should allow the first-name or last-name or nickname to be empty or blank`(name: String) {
        val contactFirst = someContactEditable(firstName = "Test", lastName = name, nickname = name)
        val contactLast = someContactEditable(firstName = name, lastName = "Test", nickname = name)
        val contactNick = someContactEditable(firstName = name, lastName = name, nickname = "Test")

        val resultFirst = runBlocking { underTest.validateContact(contactLast) }
        val resultLast = runBlocking { underTest.validateContact(contactFirst) }
        val resultNick = runBlocking { underTest.validateContact(contactNick) }

        assertThat(resultFirst).isInstanceOf(Success::class.java)
        assertThat(resultLast).isInstanceOf(Success::class.java)
        assertThat(resultNick).isInstanceOf(Success::class.java)
    }
}
