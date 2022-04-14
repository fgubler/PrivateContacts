/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.util.injectAnywhere

object Settings {
    private val provider: SettingsProvider by injectAnywhere()
    val current = provider
}
