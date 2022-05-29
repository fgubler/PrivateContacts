/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.settings

import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.Flow

object Settings {
    val repository: SettingsRepository by injectAnywhere()

    val current: ISettingsState
        get() = repository

    val flow: Flow<ISettingsState>
        get() = repository.settings

    fun restoreDefaultSettings() {
        repository.overrideSettingsWith(SettingsState.defaultSettings)
    }
}
