/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsState

@Composable
fun Settings.observeAsState() = repository.settings.collectAsState(initial = SettingsState.defaultSettings)
