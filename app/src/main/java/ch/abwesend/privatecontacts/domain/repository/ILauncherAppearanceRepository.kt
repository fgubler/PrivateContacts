/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.appearance.LauncherAppearance

interface ILauncherAppearanceRepository {
    fun setLauncherAppearance(launcherAppearance: LauncherAppearance)
}
