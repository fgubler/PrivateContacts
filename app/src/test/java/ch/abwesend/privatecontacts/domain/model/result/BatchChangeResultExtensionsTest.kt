/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.result

import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError.NAME_NOT_SET
import ch.abwesend.privatecontacts.domain.model.result.batch.BatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.BatchChangeResultWithErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.flattenedErrors
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class BatchChangeResultExtensionsTest : TestBase() {
    private val flatteningBaseData = BatchChangeResultWithErrors<Int, ContactChangeError, ContactValidationError> (
        successfulChanges = emptyList(),
        failedChanges = mapOf(
            0 to BatchChangeErrors(
                errors = listOf(UNKNOWN_ERROR, UNABLE_TO_DELETE_CONTACT),
                validationErrors = emptyList(),
            ),
            1 to BatchChangeErrors(errors = emptyList(), validationErrors = emptyList()),
            2 to BatchChangeErrors(
                errors = listOf(NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS, UNABLE_TO_DELETE_CONTACT),
                validationErrors = listOf(NAME_NOT_SET)
            ),
            3 to BatchChangeErrors(
                errors = emptyList(),
                validationErrors = listOf(NAME_NOT_SET),
            )
        )
    )

    @Test
    fun `flattenErrors should not change the number of errors`() {
        val numberOfErrors = flatteningBaseData.failedChanges.map { it.value.errors.size }.sum()
        val numberOfValidationErrors = flatteningBaseData.failedChanges.map { it.value.validationErrors.size }.sum()

        val result = flatteningBaseData.flattenedErrors()

        assertThat(result.errors).hasSize(numberOfErrors)
        assertThat(result.validationErrors).hasSize(numberOfValidationErrors)
    }

    @Test
    fun `flattenErrors should get all errors`() {
        val errors = flatteningBaseData.failedChanges.flatMap { it.value.errors }
        val validationErrors = flatteningBaseData.failedChanges.flatMap { it.value.validationErrors }

        val result = flatteningBaseData.flattenedErrors()

        assertThat(result.errors).containsExactlyElementsOf(errors)
        assertThat(result.validationErrors).containsExactlyElementsOf(validationErrors)
    }
}
