/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport.googledrive

import androidx.annotation.StringRes
import ch.abwesend.privatecontacts.R

enum class GoogleDriveSetupError(@StringRes val errorMessageRes: Int) {
    AUTHORIZATION_FAILED(R.string.drive_backup_setup_error_authorization),
    CONSENT_FAILED(R.string.drive_backup_setup_error_consent),
    EMAIL_RETRIEVAL_FAILED(R.string.drive_backup_setup_error_email),
    FOLDER_ACCESS_CHECK_FAILED(R.string.drive_backup_setup_error_folder_access),
    FOLDER_CREATION_FAILED(R.string.drive_backup_setup_error_folder_creation),
    UNKNOWN(R.string.drive_backup_setup_error_unknown),
}
