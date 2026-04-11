/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport.googledrive

import android.app.PendingIntent

sealed interface GoogleDriveAuthResult<out T> {
    data class ConsentRequired(val pendingIntent: PendingIntent) : GoogleDriveAuthResult<Nothing>
    data class Authorized<T>(val data: T) : GoogleDriveAuthResult<T>
    data object Error : GoogleDriveAuthResult<Nothing>
}
