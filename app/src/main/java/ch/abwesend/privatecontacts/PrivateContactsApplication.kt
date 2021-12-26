package ch.abwesend.privatecontacts

import android.app.Application
import android.content.Context
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.ContactLoadService
import ch.abwesend.privatecontacts.domain.service.IContactLoadService
import ch.abwesend.privatecontacts.infrastructure.ContactRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val koinModule = module {
    single<IContactLoadService> { ContactLoadService() }
    single<IContactRepository> { ContactRepository() }
}

class PrivateContactsApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this

        startKoin {
            androidLogger()
            androidContext(context)
            modules(koinModule)
        }
    }
}
