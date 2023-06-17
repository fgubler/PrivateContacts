/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.content.res.Resources
import androidx.annotation.StringRes

fun interface StringProvider {
    operator fun invoke(@StringRes stringRes: Int): String
}

internal class ResourcesBasedStringProvider(private val resources: Resources) : StringProvider {
    override fun invoke(stringRes: Int): String = resources.getString(stringRes)
}
