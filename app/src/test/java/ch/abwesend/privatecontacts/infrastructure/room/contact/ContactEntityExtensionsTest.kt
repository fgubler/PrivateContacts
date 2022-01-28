package ch.abwesend.privatecontacts.infrastructure.room.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.someContactFull
import ch.abwesend.privatecontacts.testutil.somePhoneNumber
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactDataRepositoryTest : TestBase() {
    @Test
    fun `fulltext search string should contain the names`() {
        val contact = someContactFull()

        val entity = contact.toEntity()

        assertThat(entity.fullTextSearch).containsSequence(contact.firstName)
        assertThat(entity.fullTextSearch).containsSequence(contact.lastName)
        assertThat(entity.fullTextSearch).containsSequence(contact.nickname)
    }

    @Test
    fun `fulltext search string should contain the notes`() {
        val contact = someContactFull()

        val entity = contact.toEntity()

        assertThat(entity.fullTextSearch).containsSequence(contact.notes)
    }

    @Test
    fun `fulltext search string should contain the phone numbers`() {
        val contact = someContactFull(
            contactData = listOf(
                somePhoneNumber(value = "12345"),
                somePhoneNumber(value = "56789"),
            )
        )

        val entity = contact.toEntity()

        contact.contactDataSet.forEach { phoneNumber ->
            assertThat(entity.fullTextSearch).containsSequence((phoneNumber as PhoneNumber).value)
        }
    }
}
