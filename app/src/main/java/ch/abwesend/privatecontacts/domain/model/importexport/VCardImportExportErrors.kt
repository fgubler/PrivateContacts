/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class VCardParseError(@StringRes val label: Int) {
    FILE_READING_FAILED(R.string.file_reading_failed),
    VCF_PARSING_FAILED(R.string.vcf_parsing_failed),
}

enum class VCardCreateError(@StringRes val label: Int) {
    VCF_SERIALIZATION_FAILED(R.string.vcf_serialization_failed),
    FILE_WRITING_FAILED(R.string.file_writing_failed),
}
