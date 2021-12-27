package ch.abwesend.privatecontacts.domain.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

object KoinHelper : KoinComponent

inline fun <reified T : Any> Any.injectAnywhere(): Lazy<T> =
    KoinHelper.inject()

inline fun <reified T : Any> Any.getAnywhere(): T =
    KoinHelper.get()
