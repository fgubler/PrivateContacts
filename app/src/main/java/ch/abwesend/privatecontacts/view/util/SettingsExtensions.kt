/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsState

@Composable
fun Settings.observeAsState(): State<ISettingsState> =
    flow.collectAsStateWithLifecycle(initialValue = SettingsState.defaultSettings)

@Composable
fun Settings.observeAsNullableState(): State<ISettingsState?> =
    flow.collectAsStateWithLifecycle(initialValue = null)
