/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.application

import android.app.Application
import android.content.Context
import ch.abwesend.privatecontacts.domain.ContactDetailInitializationWorkaround
import ch.abwesend.privatecontacts.domain.lib.logging.FileLogger
import ch.abwesend.privatecontacts.domain.lib.logging.LogcatLogger
import ch.abwesend.privatecontacts.domain.lib.logging.RemoteLoggingHelper
import ch.abwesend.privatecontacts.domain.service.interfaces.IBackupScheduler
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PrivateContactsApplication : Application(), KoinComponent {
    private val backupScheduler: IBackupScheduler by injectAnywhere()

    private fun createLogger(logToCrashlytics: Boolean): LogcatLogger {
        return LogcatLogger(
            loggingTag = "PrivateContacts",
            prefix = "PrivateContactsApplication",
            logToCrashlytics = { logToCrashlytics },
        )
    }

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        ContactDetailInitializationWorkaround.hasOpenedContact = false
        FileLogger.tryCleanOldLogFilesAsync(applicationContext)
        backupScheduler.schedulePeriodicBackup()
        initializeCrashlytics()
    }

    private fun initializeKoin() {
        val context: Context = this

        startKoin {
            try {
                // TODO for some reason, it crashes with a lower one: see https://github.com/InsertKoinIO/koin/issues/1188
                androidLogger(Level.ERROR) // use android logger for koin-internal logging
            } catch (e: Exception) {
                createLogger(logToCrashlytics = true)
                    .error("Failed to register android logger on koin", e)
            }
            androidContext(context)
            modules(koinModule)
        }
    }

    private fun initializeCrashlytics() {
        try {
            applicationScope.launch {
                val settings = Settings.nextOrDefault()
                val enableCrashlytics = settings.sendErrorsToCrashlytics
                RemoteLoggingHelper().enableCrashlytics(settings.sendErrorsToCrashlytics)
                createLogger(logToCrashlytics = false)
                    .info("Crashlytics ${if (enableCrashlytics) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            createLogger(logToCrashlytics = false)
                .error("Failed to initialize Crashlytics", e)
        }
    }
}
