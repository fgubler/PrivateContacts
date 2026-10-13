/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
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
class ContactDetailViewModelTest : TestBase() {
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
    fun `should load the contact on creation`() {
        val contact = someContactEditable()
        coEvery { loadService.resolveContact(contact.id) } returns contact

        val underTest = ContactDetailViewModel(contact.id)

        assertThat(underTest.selectedContact.value).isEqualTo(ReadyResource<IContact>(contact))
        coVerify { loadService.resolveContact(contact.id) }
        confirmVerified(loadService)
    }

    @Test
    fun `reloadContact should load the contact again`() {
        val contact = someContactEditable()
        coEvery { loadService.resolveContact(contact.id) } returns contact
        val underTest = ContactDetailViewModel(contact.id)

        underTest.reloadContact()

        coVerify(exactly = 2) { loadService.resolveContact(contact.id) }
        confirmVerified(loadService)
    }

    @Test
    fun `should show an error if the contact cannot be loaded`() {
        val contact = someContactEditable()
        coEvery { loadService.resolveContact(contact.id) } throws IllegalArgumentException("Test")

        val underTest = ContactDetailViewModel(contact.id)

        assertThat(underTest.selectedContact.value).isInstanceOf(ErrorResource::class.java)
    }
}
