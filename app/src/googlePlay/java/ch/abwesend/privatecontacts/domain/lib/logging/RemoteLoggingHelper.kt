package ch.abwesend.privatecontacts.domain.lib.logging

import ch.abwesend.privatecontacts.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

interface IRemoteLoggingHelper {
    fun logErrorToCrashlytics(t: Throwable)
    fun logMessageToCrashlytics(message: String)
}

class RemoteLoggingHelper : IRemoteLoggingHelper {
    override fun logErrorToCrashlytics(t: Throwable) {
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }

    override fun logMessageToCrashlytics(message: String) {
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().log(message)
        }
    }
}
