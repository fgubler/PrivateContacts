/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import android.content.Intent
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupData
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

/**
 * Abstracts Google Drive authorization and file operations.
 * Uses the modern AuthorizationClient API (Identity Services) instead of the deprecated GoogleSignIn API.
 */
interface IGoogleDriveRepository {
    /**
     * Requests authorization for Google Drive access.
     * @return [GoogleDriveAuthResult.ConsentRequired] if user consent is needed,
     *         [GoogleDriveAuthResult.Authorized] if authorization was granted silently and folder was created,
     *         [GoogleDriveAuthResult.Error] if something went wrong.
     */
    suspend fun requestAuthorization(): GoogleDriveAuthResult

    /**
     * Handles the result Intent returned after the user completes the authorization consent flow.
     * @return [GoogleDriveSetupData] on success, or the [Exception] on failure.
     */
    suspend fun handleAuthorizationResult(data: Intent?): BinaryResult<GoogleDriveSetupData, Exception>
}
