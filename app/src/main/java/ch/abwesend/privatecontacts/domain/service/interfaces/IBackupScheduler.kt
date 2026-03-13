/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

interface IBackupScheduler {
    fun schedulePeriodicBackup()
    fun triggerOneTimeBackup()
}
