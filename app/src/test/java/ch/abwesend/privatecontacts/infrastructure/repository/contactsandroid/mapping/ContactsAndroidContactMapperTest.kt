/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.infrastructure.service.AndroidContactCompanyMappingService
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidName
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidNickname
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidNote
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidOrganization
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidPhone
import ch.abwesend.privatecontacts.testutil.databuilders.someContactsAndroidRawContact
import contacts.core.Contacts
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactsAndroidContactMapperTest : TestBase() {
    private val addressFormattingService: IAddressFormattingService = AddressFormattingService()
    private val companyMappingService = AndroidContactCompanyMappingService()
    private val contactDataMapper = ContactsAndroidDataMapper()
    private val contactsApi: Contacts = mockk(relaxed = true)

    private lateinit var underTest: ContactsAndroidContactMapper

    override fun setup() {
        super.setup()
        underTest = ContactsAndroidContactMapper()
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single<IAddressFormattingService> { addressFormattingService }
        module.single { companyMappingService }
        module.single { contactDataMapper }
        module.single { contactsApi }
    }

    // ── toContactBase ──────────────────────────────────────────────────────────

    @Test
    fun `should create a ContactBase`() {
        val contact = someContactsAndroidContact(
            id = 433L,
            displayNamePrimary = "Darth Vader",
            lookupKey = "lookup-433",
        )

        val result = underTest.toContactBase(contact = contact, rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat((result.id as IContactIdExternal).contactNo).isEqualTo(433L)
        assertThat(result.displayName).isEqualTo("Darth Vader")
        assertThat(result.type).isEqualTo(ContactType.PUBLIC)
    }

    @Test
    fun `should return null on error when rethrowExceptions is false`() {
        val contact = mockk<contacts.core.entities.Contact>(relaxed = true) {
            every { lookupKey } throws RuntimeException("test error")
        }

        val result = underTest.toContactBase(contact = contact, rethrowExceptions = false)

        assertThat(result).isNull()
    }

    // ── toContactWithPhoneNumbers ──────────────────────────────────────────────

    @Test
    fun `should create ContactWithPhoneNumbers`() {
        val phones = listOf(
            someContactsAndroidPhone(number = "+41791234567"),
            someContactsAndroidPhone(id = 2L, number = "+41791234568"),
        )
        val rawContact = someContactsAndroidRawContact(phones = phones)
        val contact = someContactsAndroidContact(
            id = 500L,
            rawContacts = listOf(rawContact),
        )

        val result = underTest.toContactWithPhoneNumbers(contact = contact, rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.phoneNumbers).hasSize(2)
        assertThat(result.phoneNumbers.map { it.value })
            .containsExactly("+41791234567", "+41791234568")
    }

    // ── toContact ──────────────────────────────────────────────────────────────

    @Test
    fun `should create a full Contact`() {
        val name = someContactsAndroidName(
            givenName = "Gabriel",
            familyName = "De Leon",
            middleName = "M",
            prefix = "Mr",
            suffix = "Jr",
        )
        val nickname = someContactsAndroidNickname("Black Lion")
        val note = someContactsAndroidNote("likes silver")
        val rawContact = someContactsAndroidRawContact(
            name = name,
            nickname = nickname,
            note = note,
        )
        val contact = someContactsAndroidContact(
            id = 433L,
            rawContacts = listOf(rawContact),
        )
        val groups = listOf(
            ContactGroup(id = ContactGroupId(name = "Group 1", groupNo = 1L), notes = "", modelStatus = ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED),
            ContactGroup(id = ContactGroupId(name = "Group 2", groupNo = 2L), notes = "", modelStatus = ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED),
        )

        val result = underTest.toContact(
            contact = contact,
            groups = groups,
            rethrowExceptions = true,
        )

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat((result.id as IContactIdExternal).contactNo).isEqualTo(433L)
        assertThat(result.firstName).isEqualTo("Gabriel")
        assertThat(result.lastName).isEqualTo("De Leon")
        assertThat(result.nickname).isEqualTo("Black Lion")
        assertThat(result.middleName).isEqualTo("M")
        assertThat(result.namePrefix).isEqualTo("Mr")
        assertThat(result.nameSuffix).isEqualTo("Jr")
        assertThat(result.notes).isEqualTo("likes silver")
        assertThat(result.contactGroups).hasSize(2)
        assertThat(result.contactGroups.map { it.id.name }).containsExactly("Group 1", "Group 2")
    }

    @Test
    fun `should use nickname as firstName when both names are empty`() {
        val name = someContactsAndroidName(givenName = "", familyName = "")
        val nickname = someContactsAndroidNickname("Black Lion")
        val rawContact = someContactsAndroidRawContact(name = name, nickname = nickname)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.toContact(contact = contact, groups = emptyList(), rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.firstName).isEqualTo("Black Lion")
    }

    @Test
    fun `should use organization as firstName when names and nickname are empty`() {
        val name = someContactsAndroidName(givenName = "", familyName = "")
        val nickname = someContactsAndroidNickname("")
        val organization = someContactsAndroidOrganization(company = "Jedi Order")
        val rawContact = someContactsAndroidRawContact(
            name = name,
            nickname = nickname,
            organization = organization,
        )
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.toContact(contact = contact, groups = emptyList(), rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.firstName).isEqualTo("Jedi Order")
    }

    @Test
    fun `should add organization as company to contactDataSet`() {
        val name = someContactsAndroidName(givenName = "Luke", familyName = "Skywalker")
        val organization = someContactsAndroidOrganization(company = "Rebel Alliance")
        val rawContact = someContactsAndroidRawContact(name = name, organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.toContact(contact = contact, groups = emptyList(), rethrowExceptions = true)

        assertThat(result).isNotNull
        val companies = result!!.contactDataSet.filterIsInstance<Company>()
        assertThat(companies.map { it.value }).contains("Rebel Alliance")
    }

    @Test
    fun `should add organization with job title as company`() {
        val name = someContactsAndroidName(givenName = "Luke", familyName = "Skywalker")
        val organization = someContactsAndroidOrganization(company = "Rebel Alliance", title = "Commander")
        val rawContact = someContactsAndroidRawContact(name = name, organization = organization)
        val contact = someContactsAndroidContact(rawContacts = listOf(rawContact))

        val result = underTest.toContact(contact = contact, groups = emptyList(), rethrowExceptions = true)

        assertThat(result).isNotNull
        val companies = result!!.contactDataSet.filterIsInstance<Company>()
        assertThat(companies.map { it.value }).contains("Rebel Alliance (Commander)")
    }

    @Test
    fun `should handle contact with no raw contacts`() {
        val contact = someContactsAndroidContact(rawContacts = emptyList())

        val result = underTest.toContact(contact = contact, groups = emptyList(), rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.firstName).isEmpty()
        assertThat(result.lastName).isEmpty()
    }
}
