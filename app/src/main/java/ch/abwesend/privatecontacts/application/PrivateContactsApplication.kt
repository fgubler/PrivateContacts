package ch.abwesend.privatecontacts.application

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PrivateContactsApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this

        startKoin {
            // TODO for some reason, it crashes with a lower one: see https://github.com/InsertKoinIO/koin/issues/1188
            androidLogger(Level.ERROR) // use android logger for koin-internal logging
            androidContext(context)
            modules(koinModule)
        }
    }

    companion object {
        val applicationScope by lazy { CoroutineScope(SupervisorJob()) }
    }
}

val applicationScope: CoroutineScope
    get() = PrivateContactsApplication.applicationScope
