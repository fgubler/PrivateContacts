/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.logging

interface IRemoteLoggingHelper {
    fun logErrorToCrashlytics(t: Throwable)
    fun logMessageToCrashlytics(message: String)
}
