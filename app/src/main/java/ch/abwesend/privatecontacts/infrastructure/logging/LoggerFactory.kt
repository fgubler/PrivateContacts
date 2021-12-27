package ch.abwesend.privatecontacts.infrastructure.logging

import ch.abwesend.privatecontacts.domain.logging.ILogger
import ch.abwesend.privatecontacts.domain.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.logging.LogcatLogger

private const val LOGGING_TAG = "PrivateContacts"

class LoggerFactory : ILoggerFactory {
    private val logcatLogger: LogcatLogger by lazy {
        LogcatLogger(LOGGING_TAG, logToCrashlytics = true)
    }

    override fun createLogcat(callerClass: Class<*>): ILogger {
        return logcatLogger
    }

    override fun createDefault(callerClass: Class<*>): ILogger =
        createLogcat(callerClass)
}
