/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.application

import ch.abwesend.privatecontacts.domain.lib.coroutine.ApplicationScope
import ch.abwesend.privatecontacts.domain.lib.coroutine.Dispatchers
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.repository.ContactPagerFactory
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.repository.IDatabaseRepository
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.DatabaseService
import ch.abwesend.privatecontacts.domain.service.EasterEggService
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.service.IncomingCallService
import ch.abwesend.privatecontacts.domain.service.interfaces.ITelephoneService
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.infrastructure.calldetection.CallNotificationRepository
import ch.abwesend.privatecontacts.infrastructure.calldetection.IncomingCallHelper
import ch.abwesend.privatecontacts.infrastructure.logging.LoggerFactory
import ch.abwesend.privatecontacts.infrastructure.paging.ContactPagingSource
import ch.abwesend.privatecontacts.infrastructure.repository.AndroidContactRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ContactDataRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ContactRepository
import ch.abwesend.privatecontacts.infrastructure.repository.DatabaseRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ToastRepository
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseDeletionHelper
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseFactory
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseHolder
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseInitializer
import ch.abwesend.privatecontacts.infrastructure.room.database.IDatabaseFactory
import ch.abwesend.privatecontacts.infrastructure.service.TelephoneService
import ch.abwesend.privatecontacts.infrastructure.settings.DataStoreSettingsRepository
import ch.abwesend.privatecontacts.view.permission.PermissionHelper
import ch.abwesend.privatecontacts.view.routing.AppRouter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val koinModule = module {
    // Services
    single { ContactLoadService() }
    single { ContactValidationService() }
    single { ContactSaveService() }
    single { FullTextSearchService() }
    single { IncomingCallService() }
    single { ContactSanitizingService() }
    single { EasterEggService() }
    single { DatabaseService() }
    single<ITelephoneService> { TelephoneService(androidContext()) }
    factory { PermissionHelper() } // has state => always use a new one (just to be safe)

    // Repositories
    single<IAndroidContactRepository> { AndroidContactRepository() }
    single<IContactRepository> { ContactRepository() }
    single<IDatabaseRepository> { DatabaseRepository() }
    single { ContactDataRepository() }
    single { CallNotificationRepository() }
    single { ToastRepository() }
    single<SettingsRepository> { DataStoreSettingsRepository(androidContext()) }

    // Factories
    single<ILoggerFactory> { LoggerFactory() }
    single<IDatabaseFactory<AppDatabase>> { DatabaseFactory() }
    single<ContactPagerFactory> { ContactPagingSource.Companion }

    // Helpers
    single { IncomingCallHelper() }
    single<IDispatchers> { Dispatchers }

    single { ApplicationScope() }
    factory { AppRouter(get()) }

    // Database
    single { DatabaseInitializer() }
    single { DatabaseDeletionHelper() }
    single { DatabaseHolder(androidContext()) }
}
