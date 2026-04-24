/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport.googledrive

import android.app.PendingIntent

sealed interface GoogleDriveSetupState {
    data object Inactive : GoogleDriveSetupState
    data object Loading : GoogleDriveSetupState
    data class ConsentRequired(val intent: PendingIntent) : GoogleDriveSetupState
    data class Error(val error: GoogleDriveSetupError) : GoogleDriveSetupState
}

fun GoogleDriveSetupError.toDriveSetupState(): GoogleDriveSetupState = GoogleDriveSetupState.Error(this)