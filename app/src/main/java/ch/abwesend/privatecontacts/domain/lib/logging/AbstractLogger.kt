/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.logging

import android.util.Log
import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.util.Constants

abstract class AbstractLogger : ILogger {
    private val remoteHelper: RemoteLoggingHelper = RemoteLoggingHelper()

    // ======== abstract methods ========
    protected abstract val loggingTag: String
    protected open val loggingActive: Boolean = true

    /**
     * Only the log-levels "WARN" and "ERROR" are ever sent to crashlytics
     * Beware: this needs the INTERNET permission and an API-key...
     */
    protected abstract val logToCrashlytics: () -> Boolean

    protected abstract fun verboseImpl(messages: Collection<String>)
    protected abstract fun debugImpl(messages: Collection<String>)
    protected abstract fun infoImpl(messages: Collection<String>)
    protected abstract fun warningImpl(messages: Collection<String>)
    protected abstract fun errorImpl(messages: Collection<String>)

    // ======== logging methods with arrays ========
    override fun verbose(vararg messages: String) =
        verbose(messages.toList())

    override fun debug(vararg messages: String) =
        debug(messages.toList())

    override fun info(vararg messages: String) =
        info(messages.toList())

    override fun warning(vararg messages: String) =
        warning(messages.toList())

    // ======== logging methods with collections ========
    override fun verbose(messages: Collection<String>) {
        if (checkLogLevel(Log.VERBOSE)) {
            verboseImpl(messages)
        }
    }

    override fun debug(messages: Collection<String>) {
        if (checkLogLevel(Log.DEBUG)) {
            debugImpl(messages)
        }
    }

    override fun info(messages: Collection<String>) {
        if (checkLogLevel(Log.INFO)) {
            infoImpl(messages)
        }
    }

    override fun warning(messages: Collection<String>) {
        if (checkLogLevel(Log.WARN)) {
            warningImpl(messages)
            val message = messages.joinToString(separator = Constants.linebreak)
            remoteHelper.logMessageToCrashlytics(message)
        }
    }

    // ======== logging methods with throwables ========
    override fun verbose(message: String, t: Throwable) {
        val verboseMessage = createThrowableLogMessage(t, message)
        verbose(verboseMessage)
    }

    override fun debug(message: String, t: Throwable) {
        val debugMessage = createThrowableLogMessage(t, message)
        debug(debugMessage)
    }

    override fun info(message: String, t: Throwable) {
        val infoMessage = createThrowableLogMessage(t, message)
        info(infoMessage)
    }

    override fun warning(message: String, t: Throwable) {
        val warningMessage = createThrowableLogMessage(t, message)
        if (checkLogLevel(Log.WARN)) {
            warningImpl(listOf(warningMessage))
            logToCrashlytics(t, overridePreferences = false)
        }
    }

    override fun error(message: String, t: Throwable) {
        val logMessage = createThrowableLogMessage(t, message)
        if (checkLogLevel(Log.ERROR)) {
            errorImpl(listOf(logMessage))
            logToCrashlytics(t, overridePreferences = false)
        }
    }

    override fun error(t: Throwable) {
        val logMessage = createThrowableLogMessage(t)
        if (checkLogLevel(Log.ERROR)) {
            errorImpl(listOf(logMessage))
            logToCrashlytics(t, overridePreferences = false)
        }
    }

    override fun logToCrashlytics(t: Throwable, overridePreferences: Boolean) {
        if (overridePreferences || logToCrashlytics()) {
            remoteHelper.logErrorToCrashlytics(t)
        }
    }

    private fun createThrowableLogMessage(t: Throwable, message: String? = null): String {
        val throwableMessage: String = t.message ?: "[No Throwable Message]"
        return message?.let {
            """
            $message
            Throwable of type '${t.javaClass.name}'
            Original Throwable-Message: '$throwableMessage'
            Stack-Trace: 
            ${t.stackTrace.joinToString(separator = Constants.linebreak)}
            """.trimIndent()
        } ?: throwableMessage
    }

    // ======== internal methods ========
    /**
     * can be overridden by subclasses to define another log-level mechanism
     * @return true, if messages of this level should be logged
     */
    protected open fun checkLogLevel(logLevel: Int): Boolean =
        loggingActive && (Log.isLoggable(loggingTag, logLevel) || allowDebugLoggingOnDebugBuild(logLevel))

    private fun allowDebugLoggingOnDebugBuild(logLevel: Int): Boolean =
        logLevel == Log.DEBUG && Log.isLoggable(loggingTag, Log.INFO) && BuildConfig.DEBUG
}
