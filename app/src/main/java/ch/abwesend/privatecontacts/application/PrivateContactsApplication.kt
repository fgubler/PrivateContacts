package ch.abwesend.privatecontacts.application

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class PrivateContactsApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this

        startKoin {
            androidLogger() // use android logger for koin-internal logging
            androidContext(context)
            modules(koinModule)
        }
    }
}
