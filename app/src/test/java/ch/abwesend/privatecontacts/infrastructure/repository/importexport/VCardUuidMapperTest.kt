/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.importexport

import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.PROTON_AUTOSAVE_PREFIX
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.PROTON_WEB_PREFIX
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.toUid
import ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.toUuidOrNull
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ezvcard.property.Uid
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class VCardUuidMapperTest : RepositoryTestBase() {
    @Test
    fun `should create a VCard-Uid from a UUID`() {
        val uuid = UUID.randomUUID()

        val uid = uuid.toUid()

        assertThat(uid.value).endsWith(uuid.toString())
    }

    @Test
    fun `should create a UUID from a VCard-Uid`() {
        val uid = Uid.random()

        val uuid = uid.toUuidOrNull()

        assertThat(uid.value).endsWith(uuid.toString())
    }

    @Test
    fun `should create a UUID from a Proton-Web-Uuid`() {
        val uid = Uid(PROTON_WEB_PREFIX + UUID.randomUUID().toString())

        val uuid = uid.toUuidOrNull()

        assertThat(uid.value).endsWith(uuid.toString())
    }

    @Test
    fun `should create a UUID from a Proton-Autosave-Uuid`() {
        val uid = Uid(PROTON_AUTOSAVE_PREFIX + UUID.randomUUID().toString())

        val uuid = uid.toUuidOrNull()

        assertThat(uid.value).endsWith(uuid.toString())
    }
}
