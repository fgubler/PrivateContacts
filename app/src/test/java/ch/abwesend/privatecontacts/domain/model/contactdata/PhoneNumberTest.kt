/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class PhoneNumberTest : TestBase() {
    @Test
    fun `should replace + from the prefix with 00 for search`() {
        val underTest = somePhoneNumber(value = "+41 44 123 45 67")

        val result = underTest.formatValueForSearch()

        assertThat(result).isEqualTo("0041441234567")
    }

    @Test
    fun `should remove all other non-digit characters`() {
        val underTest = somePhoneNumber(value = "+41+44-123:4_5ABC6?7")

        val result = underTest.formatValueForSearch()

        assertThat(result).isEqualTo("0041441234567")
    }
}
