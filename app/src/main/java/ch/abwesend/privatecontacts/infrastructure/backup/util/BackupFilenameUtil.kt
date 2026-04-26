/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.util

import ch.abwesend.privatecontacts.domain.model.contact.ContactType

val ContactType.backupFilenamePrefix: String
    get() = when (this) {
        ContactType.SECRET -> BackupConstants.SECRET_BACKUP_PREFIX
        ContactType.PUBLIC -> BackupConstants.PUBLIC_BACKUP_PREFIX
    }
