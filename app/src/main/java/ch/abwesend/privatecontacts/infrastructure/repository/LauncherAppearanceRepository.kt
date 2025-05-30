/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import ch.abwesend.privatecontacts.domain.model.appearance.LauncherAppearance
import ch.abwesend.privatecontacts.domain.repository.ILauncherAppearanceRepository

class LauncherAppearanceRepository(private val appContext: Context) : ILauncherAppearanceRepository {
    override fun setLauncherAppearance(launcherAppearance: LauncherAppearance) {
        val packageManager = appContext.packageManager
        val defaultAlias = ComponentName(appContext, "ch.abwesend.privatecontacts.DefaultIconAlias")
        val calculatorAlias = ComponentName(appContext, "ch.abwesend.privatecontacts.CalculatorIconAlias")

        when (launcherAppearance) {
            LauncherAppearance.DEFAULT -> {
                packageManager.updateComponents(enabledComponent = defaultAlias, disabledComponent = calculatorAlias)
            }
            LauncherAppearance.CALCULATOR -> {
                packageManager.updateComponents(enabledComponent = calculatorAlias, disabledComponent = defaultAlias)
            }
        }
    }

    private fun PackageManager.updateComponents(
        enabledComponent: ComponentName,
        disabledComponent: ComponentName
    ) {
        setComponentEnabledSetting(
            enabledComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        setComponentEnabledSetting(
            disabledComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
