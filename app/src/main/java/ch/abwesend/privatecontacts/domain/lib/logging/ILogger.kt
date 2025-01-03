/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.logging

interface ILogger {
    // ======== logging methods with arrays ========
    fun verbose(vararg messages: String)
    fun debug(vararg messages: String)
    fun info(vararg messages: String)
    fun warning(vararg messages: String)

    // ======== logging methods with collections ========
    fun verbose(messages: Collection<String>)
    fun debug(messages: Collection<String>)
    fun info(messages: Collection<String>)
    fun warning(messages: Collection<String>)

    // ======== logging methods with throwables ========
    fun verbose(message: String, t: Throwable)
    fun debug(message: String, t: Throwable)
    fun info(message: String, t: Throwable)
    fun warning(message: String, t: Throwable)
    fun error(message: String, t: Throwable)
    fun error(t: Throwable)
    fun logToCrashlytics(t: Throwable, overridePreferences: Boolean)
}

/**
 * Inline to make sure the stack-trace starts at the right place.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ILogger.error(message: String) {
    error(message, LoggingException(message))
}

/** log a message which must not be sent to Firebase for privacy-reasons */
fun ILogger.debugLocally(message: String) = debug(message)
fun ILogger.debugLocally(message: String, t: Throwable) = debug(message, t)
