/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import android.content.Intent
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAccessToken
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupData
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

/**
 * Abstracts Google Drive authorization and file operations.
 * Uses the modern AuthorizationClient API (Identity Services) instead of the deprecated GoogleSignIn API.
 */
interface IGoogleDriveRepository {
    /** Requests authorization for Google Drive access and runs [block] with the resulting access token. */
    suspend fun <T> runWithAuthorization(
        block: suspend (GoogleDriveAccessToken) -> BinaryResult<T, Exception>
    ): GoogleDriveAuthResult<T>

    /**
     * After [runWithAuthorization] had returned [GoogleDriveAuthResult.ConsentRequired], the consent-dialog was shown.
     * Here, we handle the result from that consent flow.
     */
    suspend fun <T> runWithAuthorizationFromIntent(
        data: Intent?,
        block: suspend (GoogleDriveAccessToken) -> BinaryResult<T, Exception>,
    ): BinaryResult<T, Exception>

    /** Creates a new folder in Google Drive for backups. */
    suspend fun createBackupFolder(accessToken: GoogleDriveAccessToken): BinaryResult<GoogleDriveSetupData, Exception>
}
