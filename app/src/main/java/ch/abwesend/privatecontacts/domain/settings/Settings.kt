/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.util.injectAnywhere

// TODO solve differently... (maybe only in view-layer and then with composables?)
object Settings {
    val repository: SettingsRepository by injectAnywhere()
}
