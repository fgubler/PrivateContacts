package ch.abwesend.privatecontacts.domain.model.importexport

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

/**
 * The version of the vcard format.
 * Beware: [V3] does not yet support some fields (which means that they are lost):
 *  - all relationships
 *  - anniversaries
 * Beware: [V4] is not supported by the Google Contacts app (it seems).
 */
enum class VCardVersion(@StringRes val label: Int) {
    V3(R.string.vcard_v3_label),
    V4(R.string.vcard_v4_label),
}
