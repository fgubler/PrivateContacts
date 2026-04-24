/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository

import android.content.Context
import android.content.Intent
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository

class GoogleDriveAuthorizationRepository(private val context: Context) : IGoogleDriveAuthorizationRepository {
    override suspend fun clearAuthorization(): BinaryResult<Unit, Exception> =
        ErrorResult(UnsupportedOperationException("Google Drive is not available in the F-Droid build"))

    override suspend fun authorize(): GoogleDriveAuthResult<IGoogleDriveRepository> =
        GoogleDriveAuthResult.Error

    override suspend fun authorizeFromIntent(data: Intent?): BinaryResult<IGoogleDriveRepository, Exception> =
        ErrorResult(UnsupportedOperationException("Google Drive is not available in the F-Droid build"))
}
