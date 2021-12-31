package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.testutil.KoinTestBase
import ch.abwesend.privatecontacts.testutil.someContactFull
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactRepositoryTest : KoinTestBase() {
    @MockK
    private lateinit var contactDataRepository: ContactDataRepository

    private lateinit var underTest: ContactRepository

    override fun setup() {
        coEvery { contactDao.insert(any()) } returns Unit
        underTest = ContactRepository()
    }

    override fun Module.setupKoinModule() {
        single { contactDataRepository }
    }

    @Test
    fun `creating a contact should also create contact data`() {
        val contact = someContactFull()
        coEvery { contactDataRepository.createContactData(any()) } returns Unit

        runBlocking { underTest.createContact(contact) }

        coVerify { contactDataRepository.createContactData(contact) }
    }
}
