/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow

// TODO solve differently... (maybe only in view-layer and then with composables?)
object Settings {
    val repository: SettingsRepository by injectAnywhere()

    val current: ISettingsState
        get() = repository.currentSettings

    val flow: Flow<ISettingsState>
        get() = repository.settings

    fun restoreDefaultSettings() {
        repository.overrideSettingsWith(SettingsState.defaultSettings)
    }
}
