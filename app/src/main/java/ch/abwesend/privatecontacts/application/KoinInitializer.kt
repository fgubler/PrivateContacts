package ch.abwesend.privatecontacts.application

import android.content.Context
import ch.abwesend.privatecontacts.domain.lib.logging.LogcatLogger
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

object KoinInitializer {
    private var koinInitialized = false

    @Synchronized
    fun initializeKoin(context: Context) {
        if (koinInitialized || GlobalContext.getOrNull() != null) {
            logger.debug("Koin is already initialized, skipping initialization")
            return
        }

        startKoin {
            try {
                // TODO for some reason, it crashes with a lower one: see https://github.com/InsertKoinIO/koin/issues/1188
                androidLogger(Level.ERROR) // use android logger for koin-internal logging
            } catch (e: Exception) {
                LogcatLogger("PrivateContactsApplication", { true })
                    .error("Failed to register android logger on koin", e)
            }
            androidContext(context)
            modules(koinModule)
        }
        logger.debug("Koin initialized successfully")
    }
}