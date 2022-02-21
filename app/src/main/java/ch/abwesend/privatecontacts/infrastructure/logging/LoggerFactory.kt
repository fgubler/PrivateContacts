/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.logging

import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.lib.logging.LogcatLogger

private const val LOGGING_TAG = "PrivateContacts"

class LoggerFactory : ILoggerFactory {
    override fun createLogcat(callerClass: Class<*>): ILogger {
        return LogcatLogger(
            loggingTag = LOGGING_TAG,
            logToCrashlytics = !BuildConfig.DEBUG,
            prefix = callerClass.simpleName,
        )
    }

    override fun createDefault(callerClass: Class<*>): ILogger =
        createLogcat(callerClass)
}
