package ch.abwesend.privatecontacts.view

import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.somePhoneNumber
import ch.abwesend.privatecontacts.view.util.addOrReplace
import ch.abwesend.privatecontacts.view.util.prepareForDisplay
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactViewExtensionsTest : TestBase() {
    @Test
    fun `prepareForDisplay should add an empty-entry to an empty list`() {
        val contactDataSet = emptyList<ContactData>()

        val result = contactDataSet.prepareForDisplay { PhoneNumber.createEmpty(it) }

        assertThat(result).hasSize(1)
        assertThat(result.first().modelStatus).isEqualTo(ModelStatus.NEW)
        assertThat(result.first().isEmpty).isTrue
    }

    @Test
    fun `prepareForDisplay should add an empty-entry to a non-empty list`() {
        val contactDataSet = listOf(somePhoneNumber())

        val result = contactDataSet.prepareForDisplay { PhoneNumber.createEmpty(it) }

        assertThat(result).hasSize(2)
        assertThat(result.first()).isEqualTo(contactDataSet.first())
        assertThat(result.last().isEmpty).isTrue
    }

    @Test
    fun `prepareForDisplay should sort the elements with the empty one last`() {
        val contactDataSet = listOf(
            somePhoneNumber(value = "3", sortOrder = 7),
            somePhoneNumber(value = "1", sortOrder = 5),
            somePhoneNumber(value = "0", sortOrder = 3),
        )

        val result = contactDataSet.prepareForDisplay { PhoneNumber.createEmpty(it) }

        assertThat(result).hasSize(4)
        assertThat(result[0]).isEqualTo(contactDataSet[2])
        assertThat(result[1]).isEqualTo(contactDataSet[1])
        assertThat(result[2]).isEqualTo(contactDataSet[0])
        assertThat(result[3].isEmpty).isTrue
        assertThat(result[3].sortOrder).isEqualTo(8)
    }

    @Test
    fun `prepareForDisplay should remove deleted elements`() {
        val contactDataSet = listOf(
            somePhoneNumber(value = "New", modelStatus = ModelStatus.NEW),
            somePhoneNumber(value = "Unchanged", modelStatus = ModelStatus.UNCHANGED),
            somePhoneNumber(value = "Changed", modelStatus = ModelStatus.CHANGED),
            somePhoneNumber(value = "Deleted", modelStatus = ModelStatus.DELETED),
        )

        val result = contactDataSet.prepareForDisplay { PhoneNumber.createEmpty(it) }

        assertThat(result).hasSize(4)
        assertThat(result[0]).isEqualTo(contactDataSet[0])
        assertThat(result[1]).isEqualTo(contactDataSet[1])
        assertThat(result[2]).isEqualTo(contactDataSet[2])
        assertThat(result[3].isEmpty).isTrue
        assertThat(result.any { it.modelStatus == ModelStatus.DELETED }).isFalse
    }

    @Test
    fun `addOrReplace should add a new element`() {
        val phoneNumber1 = somePhoneNumber(value = "1")
        val phoneNumber2 = somePhoneNumber(value = "2")
        val list = mutableListOf<PhoneNumber>()

        list.addOrReplace(phoneNumber1)
        list.addOrReplace(phoneNumber2)

        assertThat(list).hasSize(2)
        assertThat(list).isEqualTo(listOf(phoneNumber1, phoneNumber2))
    }

    @Test
    fun `addOrReplace should replace an existing element`() {
        val id = UUID.randomUUID()
        val phoneNumber1 = somePhoneNumber(id = id, value = "1")
        val phoneNumber2 = somePhoneNumber(id = id, value = "2")
        val list = mutableListOf<PhoneNumber>()

        list.addOrReplace(phoneNumber1)
        list.addOrReplace(phoneNumber2)

        assertThat(list).hasSize(1)
        assertThat(list).isEqualTo(listOf(phoneNumber2))
    }
}
