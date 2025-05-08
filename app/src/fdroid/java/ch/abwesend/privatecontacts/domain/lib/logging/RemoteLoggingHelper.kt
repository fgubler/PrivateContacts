package ch.abwesend.privatecontacts.domain.lib.logging

class RemoteLoggingHelper : IRemoteLoggingHelper {
    override fun logErrorToCrashlytics(t: Throwable) {
        // Do nothing: crashlytics is not available
    }

    override fun logMessageToCrashlytics(message: String) {
        // Do nothing: crashlytics is not available
    }
}
