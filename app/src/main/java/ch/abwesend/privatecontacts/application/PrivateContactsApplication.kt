/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.application

import android.app.Application
import android.content.Context
import ch.abwesend.privatecontacts.domain.lib.logging.LogcatLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PrivateContactsApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        ContactDetailInitializationWorkaround.hasOpenedContact = false
    }

    private fun initializeKoin() {
        val context: Context = this

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
    }
}
