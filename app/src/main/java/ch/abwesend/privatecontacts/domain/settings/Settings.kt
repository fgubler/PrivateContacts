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

    /** be careful about using that - especially on app-start: it might just be the default */
    val current: ISettingsState
        get() = repository

    val flow: Flow<ISettingsState>
        get() = repository.settings

    suspend fun nextOrDefault(): ISettingsState {
        return repository.nextSettings() ?: current
    }

    fun restoreDefaultSettings() {
        repository.overrideSettingsWith(SettingsState.defaultSettings)
    }
}
