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

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { loadService }
    }

    override fun setup() {
        super.setup()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain()
    }

    @Test
    fun `should create a new contact`() {
        val underTest = ContactEditViewModel(ContactEditTarget.New)

        val selectedContact = underTest.selectedContact
        assertThat(selectedContact).isNotNull
        assertThat(selectedContact!!.isNew).isTrue
        assertThat(underTest.originalContact).isEqualTo(selectedContact.contact)
        confirmVerified(loadService)
    }

    @Test
    fun `should select the unsaved contact`() {
        val contact = someContactEditable(firstName = "Arya", isNew = true)

        val underTest = ContactEditViewModel(ContactEditTarget.Unsaved(contact))

        assertThat(underTest.selectedContact?.id).isEqualTo(contact.id)
        assertThat(underTest.selectedContact?.firstName).isEqualTo("Arya")
        confirmVerified(loadService)
    }

    @Test
    fun `should load an existing contact`() {
        val contact = someContactEditable(firstName = "Sansa")
        coEvery { loadService.resolveContact(contact.id) } returns contact

        val underTest = ContactEditViewModel(ContactEditTarget.Existing(contact.id))

        assertThat(underTest.selectedContact?.id).isEqualTo(contact.id)
        assertThat(underTest.selectedContact?.firstName).isEqualTo("Sansa")
        coVerify { loadService.resolveContact(contact.id) }
        confirmVerified(loadService)
    }

    @Test
    fun `changes should not affect the original contact`() {
        val underTest = ContactEditViewModel(ContactEditTarget.New)
        val changedContact = underTest.selectedContact!!.deepCopy()
        changedContact.firstName = "Changed"

        underTest.changeContact(changedContact)

        assertThat(underTest.selectedContact?.firstName).isEqualTo("Changed")
        assertThat(underTest.originalContact?.firstName).isNotEqualTo("Changed")
    }

    @Test
    fun `should show an error if an existing contact cannot be loaded`() {
        val contact = someContactEditable()
        coEvery { loadService.resolveContact(contact.id) } throws IllegalArgumentException("Test")

        val underTest = ContactEditViewModel(ContactEditTarget.Existing(contact.id))

        assertThat(underTest.selectedContact).isNull()
        assertThat(underTest.contactLoadingState.value).isInstanceOf(ErrorResource::class.java)
    }
}
