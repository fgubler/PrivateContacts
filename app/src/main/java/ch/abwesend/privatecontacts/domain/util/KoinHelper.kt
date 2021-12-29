package ch.abwesend.privatecontacts.domain.util

import ch.abwesend.privatecontacts.domain.lib.coroutine.ApplicationScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

object KoinHelper : KoinComponent

inline fun <reified T : Any> injectAnywhere(): Lazy<T> =
    KoinHelper.inject()

inline fun <reified T : Any> getAnywhere(): T =
    KoinHelper.get()

val applicationScope: ApplicationScope
    get() = getAnywhere()
