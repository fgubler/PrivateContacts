/*
 * Swiss Drone Maps
 * Copyright (c) 2021
 * Florian Gubler & Raphael Gubler
 */
package ch.abwesend.privatecontacts.domain.lib.logging

class CombinedLogger(
    private val subLoggers: List<ILogger>,
    override val loggingTag: String,
    override val logToCrashlytics: () -> Boolean
) : AbstractLogger() {
    override fun verboseImpl(messages: Collection<String>) {
        subLoggers.forEach {
            kotlin.runCatching { it.verbose(messages)  }
        }
    }

    override fun debugImpl(messages: Collection<String>) {
        subLoggers.forEach {
            kotlin.runCatching { it.debug(messages) }
        }
    }

    override fun infoImpl(messages: Collection<String>) {
        subLoggers.forEach {
            kotlin.runCatching { it.info(messages) }
        }
    }

    override fun warningImpl(messages: Collection<String>) {
        subLoggers.forEach { kotlin.runCatching { it.warning(messages) } }
    }

    override fun errorImpl(messages: Collection<String>) {
        subLoggers.forEach { subLogger ->
            kotlin.runCatching {
                messages.forEach { message ->
                    subLogger.error(message)
                }
            }
        }
    }
}