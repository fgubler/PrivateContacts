/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import androidx.annotation.StringRes

fun interface StringProvider {
    operator fun invoke(@StringRes stringRes: Int): String
}
