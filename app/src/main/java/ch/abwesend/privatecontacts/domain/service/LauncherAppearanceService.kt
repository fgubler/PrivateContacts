/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.model.appearance.LauncherAppearance
import ch.abwesend.privatecontacts.domain.repository.ILauncherAppearanceRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class LauncherAppearanceService {
    private val repository: ILauncherAppearanceRepository by injectAnywhere()

    fun useCalculatorAppearance() {
        repository.setLauncherAppearance(LauncherAppearance.CALCULATOR)
    }

    fun useDefaultAppearance() {
        repository.setLauncherAppearance(LauncherAppearance.DEFAULT)
    }
}
