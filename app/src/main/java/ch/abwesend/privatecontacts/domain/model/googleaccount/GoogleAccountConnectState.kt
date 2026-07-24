/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.googleaccount

import android.app.PendingIntent
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError

sealed interface GoogleAccountConnectIntermediateState {
    data class Success(val accountEmail: String) : GoogleAccountConnectIntermediateState
}

sealed interface GoogleAccountConnectState : GoogleAccountConnectIntermediateState {
    data object Inactive : GoogleAccountConnectState
    data object Loading : GoogleAccountConnectState
    data class ConsentRequired(val intent: PendingIntent) : GoogleAccountConnectState
    data class Error(val error: GoogleDriveSetupError) : GoogleAccountConnectState
}

fun GoogleDriveSetupError.toGoogleAccountConnectState(): GoogleAccountConnectState =
    GoogleAccountConnectState.Error(this)
