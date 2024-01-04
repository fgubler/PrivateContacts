/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping.contactdata.export

import ezvcard.parameter.ImageType

/** I am not sure whether this actually works but the images are exported and re-imported successfully, so... */
object ImageTypeDetector {
    private val pngHeader = byteArrayOf(
        0x89.toByte(),
        'P'.code.toByte(),
        'N'.code.toByte(),
        'G'.code.toByte(),
        0x0D.toByte(),
        0x0A.toByte(),
        0x1A.toByte(),
        0x0A.toByte()
    )

    private val jpgHeader = byteArrayOf(
        0xFF.toByte(),
        0xD8.toByte(),
        0xFF.toByte()
    )

    private val gifHeaders = listOf(
        "GIF87a".toByteArray(),
        "GIF89a".toByteArray(),
    )

    private val imageTypeMap: Map<ImageType, List<ByteArray>> = mapOf(
        ImageType.PNG to listOf(pngHeader),
        ImageType.JPEG to listOf(jpgHeader),
        ImageType.GIF to gifHeaders,
    )

    fun detectImageType(image: ByteArray): ImageType? {
        return imageTypeMap.filter { (_, typeHeaders) ->
            typeHeaders.any { typeHeader ->
                val headerLength = typeHeader.size
                val imageStart = image.take(headerLength)
                imageStart.toByteArray().contentEquals(typeHeader)
            }
        }.keys.firstOrNull()
    }
}
