/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport.extensions

object ImportExportConstants {
    const val VCF_MAIN_MIME_TYPE = "text/x-vcard"
    const val VCF_FILE_EXTENSION = "vcf"
    val VCF_MIME_TYPES = arrayOf("text/vcard", VCF_MAIN_MIME_TYPE)

    const val CRYPT_FILE_EXTENSION = "vcf.crypt"
    const val CRYPT_MIME_TYPE = "text/plain"
    val ALL_BACKUP_MIME_TYPES = arrayOf(*VCF_MIME_TYPES, CRYPT_MIME_TYPE)
}
