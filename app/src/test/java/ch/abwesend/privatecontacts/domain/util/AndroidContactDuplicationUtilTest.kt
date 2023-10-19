package ch.abwesend.privatecontacts.domain.util

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AndroidContactDuplicationUtilTest {
    @Test
    fun `should remove duplicates`() {
        val number1 = "1234"
        val number2 = "12345"
        val number3 = "123456"
        val number4 = "1234567"
        val phoneNumbers = listOf(
            somePhoneNumber(value = number1, type = ContactDataType.Mobile),
            somePhoneNumber(value = number2, type = ContactDataType.Mobile),
            somePhoneNumber(value = number2, type = ContactDataType.Personal),
            somePhoneNumber(value = number3, type = ContactDataType.Mobile),
            somePhoneNumber(value = number3, type = ContactDataType.Personal),
            somePhoneNumber(value = number3, type = ContactDataType.Business),
            somePhoneNumber(value = number4, type = ContactDataType.Mobile),
            somePhoneNumber(value = number4, type = ContactDataType.Personal),
            somePhoneNumber(value = number4, type = ContactDataType.Business),
            somePhoneNumber(value = number4, type = ContactDataType.Other),
        )
        val expectedResult = listOf(
            phoneNumbers[0],
            phoneNumbers[1],
            phoneNumbers[3],
            phoneNumbers[6],
        )

        val result = phoneNumbers.removePhoneNumberDuplicates()

        assertThat(result).hasSameSizeAs(expectedResult)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `should remove duplicates of the same number in different formatting`() {
        val number1 = "+41 44 123 44 55"
        val number2 = "+41-44-123-44-55"
        val number3 = "+41441234455"
        val phoneNumbers = listOf(
            somePhoneNumber(value = number1, formattedValue = number2, type = ContactDataType.Mobile),
            somePhoneNumber(value = number2, formattedValue = number3, type = ContactDataType.Business),
            somePhoneNumber(value = number3, formattedValue = number1, type = ContactDataType.Other),
        )
        val expectedResult = listOf(
            phoneNumbers[0],
        )

        val result = phoneNumbers.removePhoneNumberDuplicates()

        assertThat(result).hasSameSizeAs(expectedResult)
        assertThat(result).isEqualTo(expectedResult)
    }
}
