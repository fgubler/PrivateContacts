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
}

/**
 * Inline to make sure the stack-trace starts at the right place.
 */
inline fun ILogger.error(message: String) {
    error(message, LoggingException(message))
}
