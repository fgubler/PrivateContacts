/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import android.app.PendingIntent
import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

sealed interface SyncIntermediateSetupState {
    data class Success(val accountEmail: String) : SyncIntermediateSetupState
}

sealed interface SyncSetupState : SyncIntermediateSetupState {
    data object Inactive : SyncSetupState
    data object Loading : SyncSetupState
    data class ConsentRequired(val intent: PendingIntent) : SyncSetupState
    data class Error(val error: SyncSetupError) : SyncSetupState
}

enum class SyncSetupError(@param:StringRes val errorMessageRes: Int) {
    AUTHORIZATION_FAILED(R.string.drive_backup_setup_error_authorization),
    CONSENT_FAILED(R.string.drive_backup_setup_error_consent),
    EMAIL_RETRIEVAL_FAILED(R.string.drive_backup_setup_error_email),
    WRONG_PASSPHRASE(R.string.sync_enable_wrong_passphrase_error),
    UNKNOWN(R.string.sync_enable_failed_error),
}

fun SyncSetupError.toSyncSetupState(): SyncSetupState = SyncSetupState.Error(this)
