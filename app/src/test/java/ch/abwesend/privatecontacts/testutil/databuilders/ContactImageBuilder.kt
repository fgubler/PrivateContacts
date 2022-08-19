package ch.abwesend.privatecontacts.testutil.databuilders

import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.infrastructure.room.contactimage.ContactImageEntity
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

fun someContactImageEntity(
    contactId: UUID = UUID.randomUUID(),
    thumbnailUri: String? = null,
    fullImage: ByteArray? = ByteArray(0),
): ContactImageEntity {
    val mock = mockk<ContactImageEntity>()

    every { mock.contactId } returns contactId
    every { mock.thumbnailUri } returns thumbnailUri
    every { mock.fullImage } returns fullImage

    return mock
}

fun someContactImage(
    thumbnailUri: String? = null,
    fullImage: ByteArray? = ByteArray(0),
    unchanged: Boolean = true,
): ContactImage = ContactImage(
    thumbnailUri = thumbnailUri,
    fullImage = fullImage,
    unchanged = unchanged,
)
