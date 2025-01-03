package ch.abwesend.privatecontacts.domain.lib.logging

class RemoteLoggingHelper {
    fun logErrorToCrashlytics(t: Throwable) {
        // Do nothing: crashlytics is not available
    }

    fun logMessageToCrashlytics(message: String) {
        // Do nothing: crashlytics is not available
    }
}
