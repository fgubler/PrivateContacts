/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import com.alexstyl.contactstore.Label
import com.alexstyl.contactstore.LabeledValue
import io.mockk.every
import io.mockk.spyk

fun <T : Any> T.toLabeledValue(label: Label, id: Int? = null): LabeledValue<T> {
    val labeled = spyk(LabeledValue(value = this, label = label))
    id?.let { every { labeled.id } returns id.toLong() }
    return labeled
}
