/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import android.content.Intent
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

interface IGoogleDriveAuthenticationRepository {
    /** Requests initial authorization for Google Drive access. */
    suspend fun authorize(): GoogleDriveAuthResult<IGoogleDriveRepository>

    /**
     * After [authorize] had returned [GoogleDriveAuthResult.ConsentRequired], the consent-dialog was shown.
     * Here, we handle the result from that consent flow.
     */
    suspend fun authorizeFromIntent(data: Intent?): BinaryResult<IGoogleDriveRepository, Exception>
}
