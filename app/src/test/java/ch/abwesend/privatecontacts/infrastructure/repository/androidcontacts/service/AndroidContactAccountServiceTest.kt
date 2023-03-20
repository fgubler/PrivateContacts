/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someInternetAccount
import ch.abwesend.privatecontacts.testutil.databuilders.someOnlineAccount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
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
class AndroidContactAccountServiceTest : TestBase() {
    @MockK
    private lateinit var contactLoadRepository: AndroidContactLoadRepository

    @InjectMockKs
    private lateinit var underTest: AndroidContactAccountService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactLoadRepository }
    }

    @Test
    fun `should return saveInAccount if OnlineAccount (contact is being created)`() {
        val account = someOnlineAccount()
        val contact = someContactEditable(saveInAccount = account)

        val result = runBlocking { underTest.getBestGuessForCorrespondingAccount(contact) }

        assertThat(result).isEqualTo(account)
        confirmVerified(contactLoadRepository)
    }

    @Test
    fun `should return saveInAccount if LocalPhoneContacts (contact is being created)`() {
        val account = ContactAccount.LocalPhoneContacts
        val contact = someContactEditable(saveInAccount = account)

        val result = runBlocking { underTest.getBestGuessForCorrespondingAccount(contact) }

        assertThat(result).isEqualTo(account)
        confirmVerified(contactLoadRepository)
    }

    @Test
    fun `should try guessing if saveInAccount is NONE (contact is being updated)`() {
        val account = ContactAccount.None
        val contact = someContactEditable(saveInAccount = account)
        coEvery { contactLoadRepository.loadContactGroupsByIds(any()) } returns emptyList()
        coEvery { contactLoadRepository.loadAllContactGroups() } returns emptyList()

        val result = runBlocking { underTest.getBestGuessForCorrespondingAccount(contact) }

        assertThat(result).isNotEqualTo(account)
    }

    @Test
    fun `should try guessing from other groups of the same contact`() {
        val account = ContactAccount.None
        val contactGroupNo = 777L
        val otherContactGroup = someContactGroup(groupNo = contactGroupNo)
        val contact = someContactEditable(saveInAccount = account, contactGroups = listOf(otherContactGroup))
        val expectedAccount = someInternetAccount()
        val otherGroups = listOf(someAndroidContactGroup(account = expectedAccount))
        coEvery { contactLoadRepository.loadContactGroupsByIds(any()) } returns otherGroups

        val result = runBlocking { underTest.getBestGuessForCorrespondingAccount(contact) }

        assertThat(result).isInstanceOf(ContactAccount.OnlineAccount::class.java)
        val resultAccount = result as ContactAccount.OnlineAccount
        assertThat(resultAccount.username).isEqualTo(expectedAccount.name)
        assertThat(resultAccount.accountProvider).isEqualTo(expectedAccount.type)
        coVerify { contactLoadRepository.loadContactGroupsByIds(listOf(contactGroupNo)) }
        coVerify(exactly = 0) { contactLoadRepository.loadAllContactGroups() }
    }

    @Test
    fun `should try guessing from groups of the other contacts`() {
        val account = ContactAccount.None
        val contact = someContactEditable(saveInAccount = account)
        val otherAccount = someInternetAccount(name = "other account")
        val expectedAccount = someInternetAccount(name = "main account")
        val otherGroups = listOf(
            someAndroidContactGroup(account = expectedAccount),
            someAndroidContactGroup(account = expectedAccount),
            someAndroidContactGroup(account = expectedAccount),
            someAndroidContactGroup(account = otherAccount),
            someAndroidContactGroup(account = otherAccount),
        )
        coEvery { contactLoadRepository.loadContactGroupsByIds(any()) } returns emptyList()
        coEvery { contactLoadRepository.loadAllContactGroups() } returns otherGroups

        val result = runBlocking { underTest.getBestGuessForCorrespondingAccount(contact) }

        assertThat(result).isInstanceOf(ContactAccount.OnlineAccount::class.java)
        val resultAccount = result as ContactAccount.OnlineAccount
        assertThat(resultAccount.username).isEqualTo(expectedAccount.name)
        assertThat(resultAccount.accountProvider).isEqualTo(expectedAccount.type)
        coVerify { contactLoadRepository.loadAllContactGroups() }
    }
}
