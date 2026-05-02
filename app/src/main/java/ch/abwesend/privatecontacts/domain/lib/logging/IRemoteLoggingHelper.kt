/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.logging

import android.content.Context

interface IRemoteLoggingHelper {
    fun logErrorToCrashlytics(t: Throwable)
    fun logMessageToCrashlytics(message: String)
    fun enableCrashlytics(context: Context, enable: Boolean)
}
