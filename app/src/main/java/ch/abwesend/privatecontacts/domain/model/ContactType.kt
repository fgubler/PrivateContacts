package ch.abwesend.privatecontacts.domain.model

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class ContactType(@StringRes label: Int) {
    PRIVATE(R.string.private_contact),
    PUBLIC(R.string.public_contact),
}
