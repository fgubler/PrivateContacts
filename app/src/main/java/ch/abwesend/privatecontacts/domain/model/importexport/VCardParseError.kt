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
