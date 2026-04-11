/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport.googledrive

import android.app.PendingIntent

sealed interface GoogleDriveAuthResult {
    data class ConsentRequired(val pendingIntent: PendingIntent) : GoogleDriveAuthResult
    data class Authorized(val data: GoogleDriveSetupData) : GoogleDriveAuthResult
    data object Error : GoogleDriveAuthResult
}
