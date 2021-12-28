package ch.abwesend.privatecontacts.application

import ch.abwesend.privatecontacts.domain.lib.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.IContactLoadService
import ch.abwesend.privatecontacts.infrastructure.logging.LoggerFactory
import ch.abwesend.privatecontacts.infrastructure.repository.ContactRepository
import ch.abwesend.privatecontacts.infrastructure.room.DatabaseFactory
import ch.abwesend.privatecontacts.view.routing.AppRouter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val koinModule = module {
    single<IContactLoadService> { ContactLoadService() }
    single<IContactRepository> { ContactRepository() }
    single<ILoggerFactory> { LoggerFactory() }
    factory { AppRouter(get()) }

    single {
        val context = androidContext()
        DatabaseFactory.createDatabase(context)
    }
}
