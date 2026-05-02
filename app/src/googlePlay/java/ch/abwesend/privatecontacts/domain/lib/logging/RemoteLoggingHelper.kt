package ch.abwesend.privatecontacts.domain.lib.logging

import android.content.Context
import ch.abwesend.privatecontacts.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

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

    override fun enableCrashlytics(context: Context, enable: Boolean) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enable
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enable)
    }
}
