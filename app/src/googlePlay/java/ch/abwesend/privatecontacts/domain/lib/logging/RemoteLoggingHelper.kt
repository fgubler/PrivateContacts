package ch.abwesend.privatecontacts.domain.lib.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics

class RemoteLoggingHelper {
    fun logErrorToCrashlytics(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    fun logMessageToCrashlytics(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }
}
