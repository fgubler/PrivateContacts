/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync.mapping

import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataCategory
import ch.abwesend.privatecontacts.testutil.databuilders.someCompany
import ch.abwesend.privatecontacts.testutil.databuilders.someContactEditable
import ch.abwesend.privatecontacts.testutil.databuilders.someContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someEmailAddress
import ch.abwesend.privatecontacts.testutil.databuilders.someEventDate
import ch.abwesend.privatecontacts.testutil.databuilders.someInternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.somePhoneNumber
import ch.abwesend.privatecontacts.testutil.databuilders.someRelationship
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SyncContactMapperTest {
    private val mapper = SyncContactMapper()

    @Test
    fun `round-trip preserves names, notes, contact-data and groups`() {
        val contact = someContactEditable(
            id = someInternalContactId(),
            firstName = "Jon",
            lastName = "Snow",
            nickname = "Lord Snow",
            middleName = "of",
            namePrefix = "Sir",
            nameSuffix = "III",
            type = ContactType.SECRET,
            notes = "Knows nothing",
            contactData = listOf(
                somePhoneNumber(value = "+41 79 123 45 67", sortOrder = 0, isMainNumber = true),
                someEmailAddress(value = "jon@winterfell.north", sortOrder = 1),
                someEventDate(sortOrder = 2),
                someCompany(value = "Night's Watch", sortOrder = 3),
                someRelationship(value = "Arya", sortOrder = 4),
            ),
            contactGroups = listOf(someContactGroup(name = "Starks")),
        )

        val syncContact = mapper.toSyncContact(contact, "sync-guid")
        val restored = mapper.toContact(syncContact, someInternalContactId())
        val reSerialized = mapper.toSyncContact(restored, "sync-guid")

        // re-serializing the restored contact yields a byte-identical DTO
        assertThat(reSerialized).isEqualTo(syncContact)

        assertThat(restored.firstName).isEqualTo("Jon")
        assertThat(restored.lastName).isEqualTo("Snow")
        assertThat(restored.nickname).isEqualTo("Lord Snow")
        assertThat(restored.middleName).isEqualTo("of")
        assertThat(restored.namePrefix).isEqualTo("Sir")
        assertThat(restored.nameSuffix).isEqualTo("III")
        assertThat(restored.notes).isEqualTo("Knows nothing")
        assertThat(restored.type).isEqualTo(ContactType.SECRET)
        assertThat(restored.contactGroups.map { it.id.name }).containsExactly("Starks")
        assertThat(restored.contactDataSet.map { it.category })
            .containsExactlyInAnyOrder(
                ContactDataCategory.PHONE_NUMBER,
                ContactDataCategory.EMAIL,
                ContactDataCategory.EVENT_DATE,
                ContactDataCategory.COMPANY,
                ContactDataCategory.RELATIONSHIP,
            )
    }

    @Test
    fun `serialize-deserialize round-trip of the DTO is stable`() {
        val contact = someContactEditable(
            firstName = "Ada",
            lastName = "Lovelace",
            contactData = listOf(somePhoneNumber(value = "123", sortOrder = 0)),
        )
        val syncContact = mapper.toSyncContact(contact, "guid")
        val json = mapper.serializeContact(syncContact)
        assertThat(mapper.deserializeContact(json)).isEqualTo(syncContact)
    }
}
