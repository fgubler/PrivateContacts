/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someEventDate
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class EventDateTest : TestBase() {
    @Test
    fun `should serialize date correctly`() {
        val date = LocalDate.of(2022, 5, 1)
        val underTest = someEventDate(value = date)

        val result = underTest.serializedValue()

        assertThat(result).isEqualTo("2022-05-01")
    }

    @Test
    fun `should deserialize date correctly`() {
        val date = LocalDate.of(2022, 5, 1)
        val serializedDate = "2022-05-01"

        val result = EventDate.deserializeDate(serializedDate)

        assertThat(result).isEqualTo(date)
    }
}
