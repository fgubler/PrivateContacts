package ch.abwesend.privatecontacts.domain.logging

import ch.abwesend.privatecontacts.domain.util.Constants

/**
 * An exception to log a string-message and have it sent to error-logs with the correct stack-trace.
 */
class LoggingException : Exception {
    constructor(message: String) : super(message)
    constructor(messages: List<String>) : super(
        messages
            .filter { it.isNotEmpty() }
            .joinToString(separator = Constants.linebreak)
    )
}
