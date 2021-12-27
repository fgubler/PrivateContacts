package ch.abwesend.privatecontacts.domain.logging

import android.util.Log

class LogcatLogger(
    override val loggingTag: String,
    override val logToCrashlytics: Boolean
) : AbstractLogger() {

    override fun verboseImpl(messages: Collection<String>) {
        for (message in messages) {
            Log.v(loggingTag, message)
        }
    }

    override fun debugImpl(messages: Collection<String>) {
        for (message in messages) {
            Log.d(loggingTag, message)
        }
    }

    override fun infoImpl(messages: Collection<String>) {
        for (message in messages) {
            Log.i(loggingTag, message)
        }
    }

    override fun warningImpl(messages: Collection<String>) {
        for (message in messages) {
            Log.w(loggingTag, message)
        }
    }

    override fun errorImpl(messages: Collection<String>) {
        for (message in messages) {
            Log.e(loggingTag, message)
        }
    }
}
