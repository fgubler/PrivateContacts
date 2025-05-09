/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import android.content.Context
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.FetchRequest
import com.alexstyl.contactstore.coroutines.asFlow
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

/**
 * TODO for some reason, it does not really work to add tests for resolving the list of contacts => find out why.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactLoadRepositoryTest : TestBase() {
    @MockK
    private lateinit var contactStore: ContactStore

    @MockK
    private lateinit var permissionService: PermissionService

    @RelaxedMockK
    private lateinit var context: Context

    @InjectMockKs
    private lateinit var underTest: AndroidContactLoadRepository

    private lateinit var fetchRequest: FetchRequest<List<Contact>>
    private lateinit var flow: MutableStateFlow<List<Contact>>

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactStore }
        module.single { permissionService }
        module.single { context }
    }

    override fun setup() {
        super.setup()

        fetchRequest = mockk()
        flow = MutableStateFlow(emptyList())
        mockkStatic(FetchRequest<*>::asFlow)
        every { fetchRequest.asFlow() } returns flow
        every { contactStore.fetchContacts(any(), any(), any()) } returns fetchRequest
    }

    @Test
    fun `should resolve an existing contact by ID`() {
        val contactId = ContactIdAndroid(contactNo = 123123, lookupKey = "Test")
        val contact = someAndroidContact(contactId = contactId.contactNo, relaxed = true)
        every { permissionService.hasContactReadPermission() } returns true
        runBlocking { flow.emit(listOf(contact)) }

        val result = runBlocking { underTest.resolveContactRaw(contactId) }

        coVerify { contactStore.fetchContacts(any(), any(), any()) }
        assertThat(result.contactId).isEqualTo(contactId.contactNo)
    }

    @Test
    fun `resolving a contact should throw if the contact could not be found`() {
        val contactId = ContactIdAndroid(contactNo = 123123, lookupKey = "Test")
        every { permissionService.hasContactReadPermission() } returns true
        // the flow has never emitted

        assertThrows<IllegalArgumentException> {
            runBlocking { underTest.resolveContactRaw(contactId) }
        }
    }
}
