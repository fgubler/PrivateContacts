package ch.abwesend.privatecontacts.domain.lib.logging

import ch.abwesend.privatecontacts.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

class RemoteLoggingHelper {
    fun logErrorToCrashlytics(t: Throwable) {
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }

    fun logMessageToCrashlytics(message: String) {
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().log(message)
        }
    }
}
