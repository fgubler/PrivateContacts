/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactImageRepositoryTest : RepositoryTestBase() {
    private lateinit var underTest: ContactImageRepository

    override fun setup() {
        super.setup()
        underTest = ContactImageRepository()
    }

    @Test
    fun `create should insert the data into the database`() {
//        val (contactId, contact) = someContactEditableWithId()
//        coEvery { contactDataDao.insertAll(any()) } returns Unit
//
//        runBlocking { underTest.createContactData(contactId, contact.contactDataSet) }
//
//        coVerify { contactDataDao.insertAll(any()) }
    }
}
