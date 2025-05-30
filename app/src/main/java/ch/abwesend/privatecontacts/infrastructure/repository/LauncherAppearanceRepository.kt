/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.Context
import ch.abwesend.privatecontacts.domain.model.appearance.LauncherAppearance
import ch.abwesend.privatecontacts.domain.repository.ILauncherAppearanceRepository

class LauncherAppearanceRepository(private val appContext: Context) : ILauncherAppearanceRepository {
    override fun setLauncherAppearance(launcherAppearance: LauncherAppearance) {
        TODO("Not yet implemented")
    }
}
