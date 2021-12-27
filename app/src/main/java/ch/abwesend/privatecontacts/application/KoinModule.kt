package ch.abwesend.privatecontacts.application

import ch.abwesend.privatecontacts.domain.logging.ILoggerFactory
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.IContactLoadService
import ch.abwesend.privatecontacts.infrastructure.ContactRepository
import ch.abwesend.privatecontacts.infrastructure.logging.LoggerFactory
import org.koin.dsl.module

internal val koinModule = module {
    single<IContactLoadService> { ContactLoadService() }
    single<IContactRepository> { ContactRepository() }
    single<ILoggerFactory> { LoggerFactory() }
}
