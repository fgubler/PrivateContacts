/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.util.StringProvider

class TestStringProvider(
    private val provider: (Int) -> String = { stringRes -> "Test String $stringRes" }
) : StringProvider {
    override fun invoke(stringRes: Int): String = provider(stringRes)
}
