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
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    @Test
    fun `displayValue with year set should contain the year`() {
        val date = LocalDate.of(2022, 5, 15)
        val underTest = someEventDate(value = date)

        assertThat(underTest.isYearSet).isTrue()
        assertThat(underTest.displayValue).contains("2022")
    }

    @Test
    fun `displayValue without year set should show day and month but not the year`() {
        val date = EventDate.createDate(day = 15, month = 3, year = null)!!
        val underTest = someEventDate(value = date)
        val expected = date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))

        assertThat(underTest.isYearSet).isFalse()
        assertThat(underTest.displayValue).isEqualTo(expected)
    }

    @Test
    fun `createDate should return null for an impossible date`() {
        val result = EventDate.createDate(day = 31, month = 2, year = null)

        assertThat(result).isNull()
    }

    @Test
    fun `createDate should return null for an out-of-range day`() {
        val result = EventDate.createDate(day = 32, month = 1, year = null)

        assertThat(result).isNull()
    }
}
