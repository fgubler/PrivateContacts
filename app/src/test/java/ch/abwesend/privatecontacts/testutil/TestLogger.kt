/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import ch.abwesend.privatecontacts.domain.lib.logging.AbstractLogger
import ch.abwesend.privatecontacts.domain.lib.logging.IRemoteLoggingHelper

class TestLogger : AbstractLogger(TestRemoteLoggingHelper()) {
    override val loggingTag = "Test"
    override val logToCrashlytics = { false }

    override fun verboseImpl(messages: Collection<String>) {
        for (message in messages) {
            println("Verbose: $message")
        }
    }

    override fun debugImpl(messages: Collection<String>) {
        for (message in messages) {
            println("Debug: $message")
        }
    }

    override fun infoImpl(messages: Collection<String>) {
        for (message in messages) {
            println("Info: $message")
        }
    }

    override fun warningImpl(messages: Collection<String>) {
        for (message in messages) {
            println("Warning: $message")
        }
    }

    override fun errorImpl(messages: Collection<String>) {
        for (message in messages) {
            println("Error: $message")
        }
    }

    override fun checkLogLevel(logLevel: Int): Boolean = true
}

class TestRemoteLoggingHelper : IRemoteLoggingHelper {
    override fun logErrorToCrashlytics(t: Throwable) {
        println("Test logging exception to crashlytics: ${t.message}")
        t.printStackTrace()
    }

    override fun logMessageToCrashlytics(message: String) {
        println("Test logging message to crashlytics: $message")
    }
}
