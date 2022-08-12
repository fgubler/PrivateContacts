/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contactdata

import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someEventDate
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

    @Test
    fun `should create normal date`() {
        val expectedResult = LocalDate.of(2010, 10, 1)

        val result = EventDate.createDate(day = 1, month = 10, year = 2010)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `should create date without year`() {
        val expectedResult = LocalDate.of(0, 10, 1)

        val result = EventDate.createDate(day = 1, month = 10, year = null)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `should serialize date without year`() {
        val expectedResult = "0000-10-01"
        val date = EventDate.createDate(day = 1, month = 10, year = null)!!
        val underTest = someEventDate(value = date)

        val result = underTest.serializedValue()

        assertThat(result).isEqualTo(expectedResult)
    }
}
