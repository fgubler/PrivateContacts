/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactedit

import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.view.routing.ContactEditRoute
import ch.abwesend.privatecontacts.view.routing.ContactEditTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactEditViewModelTest : TestBase() {
    @MockK
    private lateinit var loadService: ContactLoadService

    private lateinit var underTest: ContactEditViewModel

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { loadService }
    }

    override fun setup() {
        super.setup()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        underTest = ContactEditViewModel()
    }

    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize should create a new contact`() {
        underTest.initialize(ContactEditRoute(ContactEditTarget.New, instanceId = 1))

        val selectedContact = underTest.selectedContact
        assertThat(selectedContact).isNotNull
        assertThat(selectedContact!!.isNew).isTrue
        assertThat(underTest.originalContact).isEqualTo(selectedContact.contact)
        confirmVerified(loadService)
    }

    @Test
    fun `initialize should select the unsaved contact`() {
        val contact = someContactEditable(firstName = "Arya", isNew = true)

        underTest.initialize(ContactEditRoute(ContactEditTarget.Unsaved(contact), instanceId = 1))

        assertThat(underTest.selectedContact?.id).isEqualTo(contact.id)
        assertThat(underTest.selectedContact?.firstName).isEqualTo("Arya")
        confirmVerified(loadService)
    }

    @Test
    fun `initialize should load an existing contact`() {
        val contact = someContactEditable(firstName = "Sansa")
        coEvery { loadService.resolveContact(contact.id) } returns contact

        underTest.initialize(ContactEditRoute(ContactEditTarget.Existing(contact.id), instanceId = 1))

        assertThat(underTest.selectedContact?.id).isEqualTo(contact.id)
        assertThat(underTest.selectedContact?.firstName).isEqualTo("Sansa")
        coVerify { loadService.resolveContact(contact.id) }
        confirmVerified(loadService)
    }

    @Test
    fun `initialize should not discard changes for the same instance`() {
        val route = ContactEditRoute(ContactEditTarget.New, instanceId = 1)
        underTest.initialize(route)
        val changedContact = underTest.selectedContact!!.deepCopy()
        changedContact.firstName = "Changed"
        underTest.changeContact(changedContact)

        underTest.initialize(route)

        assertThat(underTest.selectedContact?.firstName).isEqualTo("Changed")
    }

    @Test
    fun `initialize should replace the contact for a new instance`() {
        val firstContact = someContactEditable(firstName = "First", isNew = true)
        val secondContact = someContactEditable(firstName = "Second", isNew = true)

        underTest.initialize(ContactEditRoute(ContactEditTarget.Unsaved(firstContact), instanceId = 1))
        underTest.initialize(ContactEditRoute(ContactEditTarget.Unsaved(secondContact), instanceId = 2))

        assertThat(underTest.selectedContact?.firstName).isEqualTo("Second")
    }

    @Test
    fun `should show an error if an existing contact cannot be loaded`() {
        val contact = someContactEditable()
        coEvery { loadService.resolveContact(contact.id) } throws IllegalArgumentException("Test")

        underTest.initialize(ContactEditRoute(ContactEditTarget.Existing(contact.id), instanceId = 1))

        assertThat(underTest.selectedContact).isNull()
        assertThat(underTest.contactLoadingState.value).isInstanceOf(ErrorResource::class.java)
    }
}
