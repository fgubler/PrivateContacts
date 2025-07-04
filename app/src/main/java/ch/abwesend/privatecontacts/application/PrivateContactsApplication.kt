/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.application

import android.app.Application
import ch.abwesend.privatecontacts.domain.ContactDetailInitializationWorkaround
import ch.abwesend.privatecontacts.domain.lib.logging.FileLogger
import org.koin.core.component.KoinComponent

class PrivateContactsApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        ContactDetailInitializationWorkaround.hasOpenedContact = false
        FileLogger.tryCleanOldLogFilesAsync(applicationContext)
    }

    private fun initializeKoin() {
        KoinInitializer.initializeKoin(this)
    }
}
