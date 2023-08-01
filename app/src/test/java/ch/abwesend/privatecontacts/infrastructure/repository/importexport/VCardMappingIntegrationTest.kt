/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.ContactToVCardMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.VCardToContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.ToPhysicalAddressMapper
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import java.util.UUID

/**
 * These tests are based on mapping a contact to a vcard and back.
 * This is a lot more convenient than just testing one direction and also guarantees
 * that no "loss" occurs during the mappings.
 */
@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VCardMappingIntegrationTest : RepositoryTestBase() {
    @InjectMockKs
    private lateinit var toVCardMapper: ContactToVCardMapper

    @InjectMockKs
    private lateinit var fromVCardMapper: VCardToContactMapper

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { ToPhysicalAddressMapper() }
        module.single<IAddressFormattingService> { AddressFormattingService() }
    }

    @Test
    fun `should map the UUID, names and notes`() {
        val uuid = UUID.randomUUID()
        val type = ContactType.SECRET
        val originalContact = someContactEditable(
            id = ContactIdInternal(uuid),
            type = type,
            notes = "This is a note"
        )

        val vCardResult = toVCardMapper.mapToVCard(originalContact)
        assertThat(vCardResult).isInstanceOf(SuccessResult::class.java)
        val vCard = vCardResult.getValueOrNull()
        assertThat(vCard).isNotNull

        val contactResult = fromVCardMapper.mapToContact(vCard!!, type)

        assertThat(contactResult).isInstanceOf(SuccessResult::class.java)
        val resultContact = contactResult.getValueOrNull()
        assertThat(resultContact).isNotNull
        assertThat(resultContact!!.importId?.value).isEqualTo(uuid)
        assertThat(resultContact.type).isEqualTo(type)
        assertThat(resultContact.firstName).isEqualTo(originalContact.firstName)
        assertThat(resultContact.lastName).isEqualTo(originalContact.lastName)
        assertThat(resultContact.nickname).isEqualTo(originalContact.nickname)
        assertThat(resultContact.notes).isEqualTo(originalContact.notes)
    }
}
