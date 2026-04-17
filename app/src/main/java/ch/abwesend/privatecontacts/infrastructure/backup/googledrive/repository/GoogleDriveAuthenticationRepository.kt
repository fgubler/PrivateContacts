/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository

import android.content.Context
import android.content.Intent
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.runCatchingAsResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthenticationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.withContext

class GoogleDriveAuthenticationRepository(private val context: Context) : IGoogleDriveAuthenticationRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    companion object {
        private const val APP_NAME = "PrivateContacts"
    }

    override suspend fun authorize(): GoogleDriveAuthResult<IGoogleDriveRepository> = withContext(dispatchers.io) {
        try {
            val authorizationResult = requestAuthorization()
            if (authorizationResult.hasResolution()) {
                authorizationResult.pendingIntent
                    ?.let { GoogleDriveAuthResult.ConsentRequired(it) }
                    ?: GoogleDriveAuthResult.Error
            } else {
                val driveRepository = authorizationResult.buildDriveRepository()
                GoogleDriveAuthResult.Authorized(data = driveRepository)
            }
        } catch (e: Exception) {
            logger.error("Failed to request authorization", e)
            GoogleDriveAuthResult.Error
        }
    }

    override suspend fun authorizeFromIntent(
        data: Intent?,
    ): BinaryResult<IGoogleDriveRepository, Exception> = withContext(dispatchers.io) {
        runCatchingAsResult {
            val result = Identity.getAuthorizationClient(context)
                .getAuthorizationResultFromIntent(data)
            result.buildDriveRepository()
        }.ifHasError { logger.error("Failed to handle authorization result", it) }
    }

    private fun buildDriveService(accessToken: GoogleDriveAccessToken): Drive {
        val initializer = HttpRequestInitializer { request ->
            request.headers.authorization = "Bearer ${accessToken.value}"
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            initializer,
        ).setApplicationName(APP_NAME).build()
    }

    private fun buildAuthorizationRequest(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
            .build()

    /**
     * Authorizes via the modern AuthorizationClient API.
     * Must be called on a background thread (uses [Tasks.await]).
     * @return the [AuthorizationResult] containing either an access token or a [android.app.PendingIntent] for consent.
     */
    private suspend fun requestAuthorization(): AuthorizationResult = withContext(dispatchers.io) {
        val client = Identity.getAuthorizationClient(context)
        Tasks.await(client.authorize(buildAuthorizationRequest()))
    }

    private fun AuthorizationResult.extractAccessToken(): GoogleDriveAccessToken {
        return accessToken?.let { GoogleDriveAccessToken(it) }
            ?: throw IllegalStateException("Authorization succeeded but no access token returned")
    }

    private fun AuthorizationResult.buildDriveRepository(): IGoogleDriveRepository {
        val token = extractAccessToken()
        val drive = buildDriveService(token)
        return GoogleDriveRepository(drive)
    }
}

@JvmInline
private value class GoogleDriveAccessToken(val value: String)