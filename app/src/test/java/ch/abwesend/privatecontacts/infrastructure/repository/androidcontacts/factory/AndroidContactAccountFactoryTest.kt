/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someInternetAccount
import ch.abwesend.privatecontacts.testutil.databuilders.someOnlineAccount
import com.alexstyl.contactstore.InternetAccount
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactAccountFactoryTest : TestBase() {
    @Test
    fun `should convert no-account to null`() {
        val account = ContactAccount.None

        val result = (account as ContactAccount).toInternetAccountOrNull()

        assertThat(result).isNull()
    }

    @Test
    fun `should convert local-contacts to null`() {
        val account = ContactAccount.LocalPhoneContacts

        val result = (account as ContactAccount).toInternetAccountOrNull()

        assertThat(result).isNull()
    }

    @Test
    fun `should convert OnlineAccount to InternetAccount`() {
        val account = someOnlineAccount()

        val result = (account as ContactAccount).toInternetAccountOrNull()

        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo(account.username)
        assertThat(result.type).isEqualTo(account.accountProvider)
    }

    @Test
    fun `should convert OnlineAccount to InternetAccount directly`() {
        val account = someOnlineAccount()

        val result = account.toInternetAccount()

        assertThat(result.name).isEqualTo(account.username)
        assertThat(result.type).isEqualTo(account.accountProvider)
    }

    @Test
    fun `should convert InternetAccount to OnlineAccount`() {
        val androidAccount = someInternetAccount()

        val result = androidAccount.toOnlineAccount()

        assertThat(result.username).isEqualTo(androidAccount.name)
        assertThat(result.accountProvider).isEqualTo(androidAccount.type)
    }

    @Test
    fun `should convert null-InternetAccount to LocalPhoneContacts`() {
        val androidAccount: InternetAccount? = null

        val result = androidAccount.toContactAccount()

        assertThat(result).isEqualTo(ContactAccount.LocalPhoneContacts)
    }
}
