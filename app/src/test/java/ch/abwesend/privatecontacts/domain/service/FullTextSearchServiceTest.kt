/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class FullTextSearchServiceTest : TestBase() {
    @InjectMockKs
    private lateinit var underTest: FullTextSearchService

    override fun Module.setupKoinModule() {
        single { underTest }
    }

    @Test
    fun `phone number - should remove all non-numerical characters`() {
        val query = "Eins1Zwei2Drei3.?!+-/_\\"

        val result = underTest.prepareQueryForPhoneNumberSearch(query)

        assertThat(result).isEqualTo("123")
    }

    @Test
    fun `fulltext search string should contain the names`() {
        val contact = someContactEditable()

        val result = underTest.computeFullTextSearchColumn(contact)

        assertThat(result).containsSequence(contact.firstName)
        assertThat(result).containsSequence(contact.lastName)
        assertThat(result).containsSequence(contact.nickname)
    }

    @Test
    fun `fulltext search string should contain the notes`() {
        val contact = someContactEditable()

        val result = underTest.computeFullTextSearchColumn(contact)

        assertThat(result).containsSequence(contact.notes)
    }

    @Test
    fun `fulltext search string should contain the phone numbers`() {
        val contact = someContactEditable(
            contactData = listOf(
                somePhoneNumber(value = "12345"),
                somePhoneNumber(value = "56789"),
            )
        )

        val result = underTest.computeFullTextSearchColumn(contact)

        contact.contactDataSet.forEach { phoneNumber ->
            assertThat(result).containsSequence((phoneNumber as PhoneNumber).value)
        }
    }

    @Test
    fun `fulltext search string should contain the email addresses`() {
        val contact = someContactEditable(
            contactData = listOf(
                someEmailAddress(value = "12345"),
                someEmailAddress(value = "56789"),
            )
        )

        val result = underTest.computeFullTextSearchColumn(contact)

        contact.contactDataSet.forEach { email ->
            assertThat(result).containsSequence((email as EmailAddress).value)
        }
    }
}
