/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.testutil.RepositoryTestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImage
import ch.abwesend.privatecontacts.testutil.databuilders.someContactImageEntity
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
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
        assertThat(result.unchanged).isTrue
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
        val image = someContactImage(unchanged = true)

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isFalse
        confirmVerified(contactImageDao)
    }

    @Test
    fun `should delete the changed image if empty`() {
        val contactId = someContactId()
        val image = spyk(someContactImage(unchanged = false))
        every { image.isEmpty } returns true
        coJustRun { contactImageDao.deleteImage(any()) }
        coJustRun { contactImageDao.insert(any()) }

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isTrue
        coVerify { contactImageDao.deleteImage(contactId.uuid) }
        confirmVerified(contactImageDao)
    }

    @Test
    fun `should replace the changed image if not empty`() {
        val contactId = someContactId()
        val image = spyk(someContactImage(unchanged = false))
        every { image.isEmpty } returns false
        coJustRun { contactImageDao.deleteImage(any()) }
        coJustRun { contactImageDao.insert(any()) }

        val result = runBlocking { underTest.storeImage(contactId, image) }

        assertThat(result).isTrue
        coVerify { contactImageDao.deleteImage(contactId.uuid) }
        coVerify { contactImageDao.insert(match { it.contactId == contactId.uuid }) }
        confirmVerified(contactImageDao)
    }
}
