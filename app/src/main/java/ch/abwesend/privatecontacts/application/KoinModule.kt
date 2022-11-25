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
import ch.abwesend.privatecontacts.domain.repository.IAddressFormattingRepository
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadService
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.repository.IContactGroupRepository
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.repository.IDatabaseRepository
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.ContactSanitizingService
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.ContactTypeChangeService
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.DatabaseService
import ch.abwesend.privatecontacts.domain.service.EasterEggService
import ch.abwesend.privatecontacts.domain.service.FullTextSearchService
import ch.abwesend.privatecontacts.domain.service.IncomingCallService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.infrastructure.calldetection.CallNotificationRepository
import ch.abwesend.privatecontacts.infrastructure.calldetection.IncomingCallHelper
import ch.abwesend.privatecontacts.infrastructure.logging.LoggerFactory
import ch.abwesend.privatecontacts.infrastructure.paging.ContactPagingSource
import ch.abwesend.privatecontacts.infrastructure.repository.ContactDataRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ContactGroupRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ContactImageRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ContactRepository
import ch.abwesend.privatecontacts.infrastructure.repository.DatabaseRepository
import ch.abwesend.privatecontacts.infrastructure.repository.ToastRepository
import ch.abwesend.privatecontacts.infrastructure.repository.addressformatting.AddressFormattingRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactLoadService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service.AndroidContactSaveService
import ch.abwesend.privatecontacts.infrastructure.room.database.AppDatabase
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseDeletionHelper
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseFactory
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseHolder
import ch.abwesend.privatecontacts.infrastructure.room.database.DatabaseInitializer
import ch.abwesend.privatecontacts.infrastructure.room.database.IDatabaseFactory
import ch.abwesend.privatecontacts.infrastructure.service.AndroidPermissionService
import ch.abwesend.privatecontacts.infrastructure.service.AndroidTelephoneService
import ch.abwesend.privatecontacts.infrastructure.settings.DataStoreSettingsRepository
import ch.abwesend.privatecontacts.view.permission.AndroidContactPermissionHelper
import ch.abwesend.privatecontacts.view.permission.CallPermissionHelper
import ch.abwesend.privatecontacts.view.permission.CallScreeningRoleHelper
import ch.abwesend.privatecontacts.view.routing.AppRouter
import com.alexstyl.contactstore.ContactStore
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
    single { ContactTypeChangeService() }
    single<TelephoneService> { AndroidTelephoneService(androidContext()) }
    single<PermissionService> { AndroidPermissionService() }
    single<IAndroidContactLoadService> { AndroidContactLoadService() }
    single { AndroidContactLoadService() }
    single<IAndroidContactSaveService> { AndroidContactSaveService() }

    single { AndroidContactPermissionHelper() } // needs to be as singleton for initialization with the Activity
    single { CallPermissionHelper() } // needs to be as singleton for initialization with the Activity
    single { CallScreeningRoleHelper() } // needs to be as singleton for initialization with the Activity

    // Repositories
    single { AndroidContactLoadRepository() }
    single { AndroidContactSaveRepository() }
    single<IContactRepository> { ContactRepository() }
    single<IDatabaseRepository> { DatabaseRepository() }
    single<IAddressFormattingRepository> { AddressFormattingRepository() }
    single<IContactGroupRepository> { ContactGroupRepository() }
    single { ContactDataRepository() }
    single { ContactGroupRepository() }
    single { ContactImageRepository() }
    single { CallNotificationRepository() }
    single { ToastRepository() }
    single<SettingsRepository> { DataStoreSettingsRepository(androidContext()) }

    // Factories
    single<ILoggerFactory> { LoggerFactory() }
    single<IDatabaseFactory<AppDatabase>> { DatabaseFactory() }

    @Suppress("DEPRECATION")
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

    // Android contacts
    single { ContactStore.newInstance(androidContext()) }
}
