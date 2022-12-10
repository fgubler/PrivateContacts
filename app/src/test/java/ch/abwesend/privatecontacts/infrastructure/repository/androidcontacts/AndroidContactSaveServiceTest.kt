/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactChangeService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactLoadService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactSaveService
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactSaveServiceTest : TestBase() {
    @MockK
    private lateinit var loadRepository: AndroidContactLoadRepository

    @MockK
    private lateinit var saveRepository: AndroidContactSaveRepository

    @MockK
    private lateinit var loadService: AndroidContactLoadService

    @MockK
    private lateinit var changeService: AndroidContactChangeService

    @InjectMockKs
    private lateinit var underTest: AndroidContactSaveService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { loadRepository }
        module.single { saveRepository }
        module.single { loadService }
        module.single { changeService }
    }

    @Test
    fun `should delete contacts and return success`() {
        val contactIds = listOf(
            ContactIdAndroid(contactNo = 123123),
            ContactIdAndroid(contactNo = 345608),
        )
        coJustRun { saveRepository.deleteContacts(contactIds) }
        coEvery { loadService.doContactsExist(any()) } answers {
            val passedContactIds = firstArg<Set<IContactIdExternal>>()
            passedContactIds.associateWith { false }
        }

        val result = runBlocking { underTest.deleteContacts(contactIds) }

        coVerify { saveRepository.deleteContacts(contactIds) }
        coVerify { loadService.doContactsExist(contactIds.toSet()) }
        assertThat(result.completelySuccessful).isTrue
    }

    @Test
    fun `should delete contacts and return partial success`() {
        val contactIds = listOf(
            ContactIdAndroid(contactNo = 123123),
            ContactIdAndroid(contactNo = 345608),
        )
        coJustRun { saveRepository.deleteContacts(contactIds) }
        coEvery { loadService.doContactsExist(any()) } answers {
            val passedContactIds = firstArg<Set<IContactIdExternal>>()
            passedContactIds.associateWith { it != passedContactIds.first() }
        }

        val result = runBlocking { underTest.deleteContacts(contactIds) }

        coVerify { saveRepository.deleteContacts(contactIds) }
        coVerify { loadService.doContactsExist(contactIds.toSet()) }
        assertThat(result.completelySuccessful).isFalse
        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[1]))
        assertThat(result.failedChanges.values.single().errors.single()).isEqualTo(UNABLE_TO_DELETE_CONTACT)
    }

    @Test
    fun `should delete contacts and look for partial success when an exception occurs`() {
        val contactIds = listOf(
            ContactIdAndroid(contactNo = 123123),
            ContactIdAndroid(contactNo = 345608),
        )
        coEvery { saveRepository.deleteContacts(contactIds) } throws IllegalStateException("Test")
        coEvery { loadService.doContactsExist(any()) } answers {
            val passedContactIds = firstArg<Set<IContactIdExternal>>()
            passedContactIds.associateWith { it != passedContactIds.first() }
        }

        val result = runBlocking { underTest.deleteContacts(contactIds) }

        coVerify { saveRepository.deleteContacts(contactIds) }
        coVerify { loadService.doContactsExist(contactIds.toSet()) }
        assertThat(result.completelySuccessful).isFalse
        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[1]))
        assertThat(result.failedChanges.values.single().errors.single()).isEqualTo(UNABLE_TO_DELETE_CONTACT)
    }

    // TODO finish implementing
//    @Test
//    fun `should update contact`() {
//        val contactId = someExternalContactId()
//        val contact = someContactEditable(id = contactId)
//        coEvery { loadRepository.resolveContactRaw(any()) } answers {
//            someAndroidContact(contactId = firstArg())
//        }
//        coEvery { loadService.resolveContact(any(), any()) } answers {
//            someContactEditable(id = firstArg())
//        }
//        coJustRun {  }
//
//        val result = runBlocking { underTest.updateContact(contactId, contact) }
//
//        coVerify { saveRepository.deleteContacts(contactIds) }
//        coVerify { loadService.doContactsExist(contactIds.toSet()) }
//        assertThat(result.completelySuccessful).isFalse
//        assertThat(result.successfulChanges).isEqualTo(listOf(contactIds[0]))
//        assertThat(result.failedChanges.keys).isEqualTo(setOf(contactIds[1]))
//        assertThat(result.failedChanges.values.single().errors.single()).isEqualTo(UNABLE_TO_DELETE_CONTACT)
//    }
}
