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

    /** during export, set octet-stream to avoid .txt file-extension */
    const val CRYPT_PRETENDING_MIME_TYPE = "application/octet-stream"

    /** in truth, it is a json-file */
    const val CRYPT_REAL_MIME_TYPE = "text/plain"

    val ALL_BACKUP_MIME_TYPES = arrayOf(*VCF_MIME_TYPES, CRYPT_PRETENDING_MIME_TYPE, CRYPT_REAL_MIME_TYPE)
}
