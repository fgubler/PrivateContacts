/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class VCardImportError(@field:StringRes val label: Int) {
    FILE_IS_EMPTY(R.string.file_is_empty),
    FILE_READING_FAILED(R.string.file_reading_failed),
    VCF_PARSING_FAILED(R.string.vcf_parsing_failed),

    DECRYPTION_FAILED_INVALID_PASSWORD(R.string.decryption_failed_invalid_password),
    DECRYPTION_FAILED_INVALID_FILE(R.string.decryption_failed_invalid_file),
    DECRYPTION_FAILED(R.string.decryption_failed),
}

enum class VCardExportError(@field:StringRes val label: Int) {
    VCF_SERIALIZATION_FAILED(R.string.vcf_serialization_failed),
    FILE_WRITING_FAILED(R.string.file_writing_failed),
    NO_CONTACTS_TO_EXPORT(R.string.no_contacts_to_export),
}
