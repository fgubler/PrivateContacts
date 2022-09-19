/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.DELETED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.ModelStatus.UNCHANGED
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImageEntity
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class ContactImageRepositoryTest : RepositoryTestBase() {
    private lateinit var underTest: ContactImageRepository

    override fun setup() {
        super.setup()
        underTest = ContactImageRepository()
    }

    @Test
    fun `should load the image for a contact`() {
        val contactId = someContactId()
        val thumbnailUri = "thumbNail"
        val fullImage = ByteArray(42)
        coEvery { contactImageDao.getImage(any()) } returns someContactImageEntity(
            contactId = contactId.uuid,
            thumbnailUri = thumbnailUri,
            fullImage = fullImage,
        )

        val result = runBlocking { underTest.loadImage(contactId) }

        coVerify { contactImageDao.getImage(contactId.uuid) }
        assertThat(result.modelStatus).isEqualTo(UNCHANGED)
        assertThat(result.thumbnailUri).isEqualTo(thumbnailUri)
        assertThat(result.fullImage).isEqualTo(fullImage)
    }

    @Test
    fun `should load empty image if none is found`() {
        val contactId = someContactId()
        coEvery { contactImageDao.getImage(any()) } returns null

        val result = runBlocking { underTest.loadImage(contactId) }

        assertThat(result).isEqualTo(ContactImage.empty)
    }

    @Test
    fun `should not change the DB if the image was not changed`() {
        val contactId = someContactId()
        val image = someContactImage(modelStatus = UNCHANGED)

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isFalse
        confirmVerified(contactImageDao)
    }

    @Test
    fun `should delete the image for status deleted`() {
        val contactId = someContactId()
        val image = someContactImage(modelStatus = DELETED)
        coJustRun { contactImageDao.delete(any()) }
        coJustRun { contactImageDao.insert(any()) }

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isTrue
        coVerify { contactImageDao.delete(match { it.contactId == contactId.uuid }) }
        confirmVerified(contactImageDao)
    }

    @Test
    fun `should replace the changed image`() {
        val contactId = someContactId()
        val image = someContactImage(modelStatus = CHANGED)
        coJustRun { contactImageDao.update(any()) }

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isTrue
        coVerify { contactImageDao.update(match { it.contactId == contactId.uuid }) }
        confirmVerified(contactImageDao)
    }

    @Test
    fun `should create a new image`() {
        val contactId = someContactId()
        val image = someContactImage(modelStatus = NEW)
        coJustRun { contactImageDao.insert(any()) }

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isTrue
        coVerify { contactImageDao.insert(match { it.contactId == contactId.uuid }) }
        confirmVerified(contactImageDao)
    }
}
