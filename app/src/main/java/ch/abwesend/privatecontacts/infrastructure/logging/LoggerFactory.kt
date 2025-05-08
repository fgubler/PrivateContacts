/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.logging

import android.content.Context
import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.lib.logging.CombinedLogger
import ch.abwesend.privatecontacts.domain.lib.logging.FileLogger
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.lib.logging.LogcatLogger
import ch.abwesend.privatecontacts.domain.settings.Settings

private const val LOGGING_TAG = "PrivateContacts"

class LoggerFactory(private val applicationContext: Context) : ILoggerFactory {

    override fun createLogcat(callerClass: Class<*>): ILogger {
        return LogcatLogger(
            loggingTag = LOGGING_TAG,
            logToCrashlytics = { !BuildConfig.DEBUG && Settings.current.sendErrorsToCrashlytics },
            prefix = callerClass.simpleName,
        )
    }

    override fun createDefault(callerClass: Class<*>): ILogger {
        val logcatLogger = createLogcat(callerClass)
        val fileLogger = FileLogger(
            context = applicationContext,
            prefix = callerClass.simpleName,
            loggingTag = LOGGING_TAG,
            logToCrashlytics = { false }, // one logger logging to crashlytics is enough
        )
        return CombinedLogger(
            subLoggers = listOf(logcatLogger, fileLogger),
            logToCrashlytics = { false }, // one logger logging to crashlytics is enough
            loggingTag = LOGGING_TAG
        )
    }
}
