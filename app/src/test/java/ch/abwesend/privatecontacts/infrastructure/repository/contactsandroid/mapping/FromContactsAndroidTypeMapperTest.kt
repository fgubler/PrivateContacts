/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping

import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import contacts.core.entities.AddressEntity
import contacts.core.entities.EmailEntity
import contacts.core.entities.EventEntity
import contacts.core.entities.PhoneEntity
import contacts.core.entities.RelationEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class FromContactsAndroidTypeMapperTest {

    // ── Phone ──────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("phoneTypeMappings")
    fun `should map phone type correctly`(type: PhoneEntity.Type?, expected: ContactDataType) {
        val result = type.toContactDataType(label = null)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should map phone CUSTOM type with label`() {
        val result = PhoneEntity.Type.CUSTOM.toContactDataType(label = "Satellite")
        assertThat(result).isEqualTo(ContactDataType.CustomValue("Satellite"))
    }

    @Test
    fun `should map phone CUSTOM type without label to Other`() {
        val result = PhoneEntity.Type.CUSTOM.toContactDataType(label = null)
        assertThat(result).isEqualTo(ContactDataType.Other)
    }

    // ── Email ──────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("emailTypeMappings")
    fun `should map email type correctly`(type: EmailEntity.Type?, expected: ContactDataType) {
        val result = type.toContactDataType(label = null)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should map email CUSTOM type with label`() {
        val result = EmailEntity.Type.CUSTOM.toContactDataType(label = "School")
        assertThat(result).isEqualTo(ContactDataType.CustomValue("School"))
    }

    // ── Address ────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("addressTypeMappings")
    fun `should map address type correctly`(type: AddressEntity.Type?, expected: ContactDataType) {
        val result = type.toContactDataType(label = null)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should map address CUSTOM type with label`() {
        val result = AddressEntity.Type.CUSTOM.toContactDataType(label = "Vacation")
        assertThat(result).isEqualTo(ContactDataType.CustomValue("Vacation"))
    }

    // ── Event ──────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("eventTypeMappings")
    fun `should map event type correctly`(type: EventEntity.Type?, expected: ContactDataType) {
        val result = type.toContactDataType(label = null)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should map event CUSTOM type with label`() {
        val result = EventEntity.Type.CUSTOM.toContactDataType(label = "Graduation")
        assertThat(result).isEqualTo(ContactDataType.CustomValue("Graduation"))
    }

    // ── Relation ───────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("relationTypeMappings")
    fun `should map relation type correctly`(type: RelationEntity.Type?, expected: ContactDataType) {
        val result = type.toContactDataType(label = null)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should map relation CUSTOM type with label`() {
        val result = RelationEntity.Type.CUSTOM.toContactDataType(label = "Mentor")
        assertThat(result).isEqualTo(ContactDataType.CustomValue("Mentor"))
    }

    companion object {
        @JvmStatic
        fun phoneTypeMappings() = listOf(
            Arguments.of(PhoneEntity.Type.MOBILE, ContactDataType.Mobile),
            Arguments.of(PhoneEntity.Type.HOME, ContactDataType.Personal),
            Arguments.of(PhoneEntity.Type.WORK, ContactDataType.Business),
            Arguments.of(PhoneEntity.Type.MAIN, ContactDataType.Main),
            Arguments.of(PhoneEntity.Type.COMPANY_MAIN, ContactDataType.Business),
            Arguments.of(PhoneEntity.Type.WORK_MOBILE, ContactDataType.MobileBusiness),
            Arguments.of(PhoneEntity.Type.FAX_WORK, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.FAX_HOME, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.PAGER, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.OTHER, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.CALLBACK, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.CAR, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.ISDN, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.OTHER_FAX, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.RADIO, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.TELEX, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.TTY_TDD, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.WORK_PAGER, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.ASSISTANT, ContactDataType.Other),
            Arguments.of(PhoneEntity.Type.MMS, ContactDataType.Other),
            Arguments.of(null, ContactDataType.Other),
        )

        @JvmStatic
        fun emailTypeMappings() = listOf(
            Arguments.of(EmailEntity.Type.HOME, ContactDataType.Personal),
            Arguments.of(EmailEntity.Type.WORK, ContactDataType.Business),
            Arguments.of(EmailEntity.Type.OTHER, ContactDataType.Other),
            Arguments.of(null, ContactDataType.Other),
        )

        @JvmStatic
        fun addressTypeMappings() = listOf(
            Arguments.of(AddressEntity.Type.HOME, ContactDataType.Personal),
            Arguments.of(AddressEntity.Type.WORK, ContactDataType.Business),
            Arguments.of(AddressEntity.Type.OTHER, ContactDataType.Other),
            Arguments.of(null, ContactDataType.Other),
        )

        @JvmStatic
        fun eventTypeMappings() = listOf(
            Arguments.of(EventEntity.Type.BIRTHDAY, ContactDataType.Birthday),
            Arguments.of(EventEntity.Type.ANNIVERSARY, ContactDataType.Anniversary),
            Arguments.of(EventEntity.Type.OTHER, ContactDataType.Other),
            Arguments.of(null, ContactDataType.Other),
        )

        @JvmStatic
        fun relationTypeMappings() = listOf(
            Arguments.of(RelationEntity.Type.BROTHER, ContactDataType.RelationshipBrother),
            Arguments.of(RelationEntity.Type.SISTER, ContactDataType.RelationshipSister),
            Arguments.of(RelationEntity.Type.CHILD, ContactDataType.RelationshipChild),
            Arguments.of(RelationEntity.Type.FATHER, ContactDataType.RelationshipFather),
            Arguments.of(RelationEntity.Type.MOTHER, ContactDataType.RelationshipMother),
            Arguments.of(RelationEntity.Type.PARENT, ContactDataType.RelationshipParent),
            Arguments.of(RelationEntity.Type.PARTNER, ContactDataType.RelationshipPartner),
            Arguments.of(RelationEntity.Type.DOMESTIC_PARTNER, ContactDataType.RelationshipPartner),
            Arguments.of(RelationEntity.Type.RELATIVE, ContactDataType.RelationshipRelative),
            Arguments.of(RelationEntity.Type.FRIEND, ContactDataType.RelationshipFriend),
            Arguments.of(RelationEntity.Type.MANAGER, ContactDataType.RelationshipWork),
            Arguments.of(RelationEntity.Type.ASSISTANT, ContactDataType.RelationshipWork),
            Arguments.of(RelationEntity.Type.SPOUSE, ContactDataType.RelationshipPartner),
            Arguments.of(RelationEntity.Type.REFERRED_BY, ContactDataType.Other),
            Arguments.of(null, ContactDataType.Other),
        )
    }
}
