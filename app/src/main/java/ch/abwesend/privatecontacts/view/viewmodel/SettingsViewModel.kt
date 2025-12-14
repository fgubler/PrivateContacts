/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.service.DatabaseService
import ch.abwesend.privatecontacts.domain.service.LauncherAppearanceService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class SettingsViewModel : ViewModel() {
    private val databaseService: DatabaseService by injectAnywhere()
    private val launcherAppearanceService: LauncherAppearanceService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()

    fun initialize(settingsRepository: SettingsRepository) {
        if (!permissionService.hasContactReadPermission()) {
            settingsRepository.blockIncomingCallsFromUnknownNumbers = false
            settingsRepository.showAndroidContacts = false
            settingsRepository.defaultContactType = ContactType.default
        }
    }

    suspend fun resetDatabase(): Boolean {
        val result = viewModelScope.async { databaseService.resetDatabase() }
        delay(2000) // let the user wait a bit
        return result.await()
    }

    fun changeLauncherAppearance(hideAppPurpose: Boolean): Boolean {
        try {
            if (hideAppPurpose) {
                launcherAppearanceService.useCalculatorAppearance()
            } else {
                launcherAppearanceService.useDefaultAppearance()
            }
            return true
        } catch (e: Exception) {
            logger.error("Failed to change launcher appearance", e)
            return false
        }
    }
}
