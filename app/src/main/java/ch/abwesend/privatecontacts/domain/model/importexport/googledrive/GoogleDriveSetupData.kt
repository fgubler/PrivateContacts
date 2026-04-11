/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport.googledrive

data class GoogleDriveSetupData(
    val accountEmail: String,
    val folderId: String,
    val folderName: String,
)
