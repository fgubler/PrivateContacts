package ch.abwesend.privatecontacts.domain.lib.logging

import android.content.Context

class RemoteLoggingHelper : IRemoteLoggingHelper {
    override fun logErrorToCrashlytics(t: Throwable) {
        // Do nothing: crashlytics is not available
    }

    override fun logMessageToCrashlytics(message: String) {
        // Do nothing: crashlytics is not available
    }

    override fun enableCrashlytics(context: Context, enable: Boolean) {
        // Do nothing: crashlytics is not available
    }
}
