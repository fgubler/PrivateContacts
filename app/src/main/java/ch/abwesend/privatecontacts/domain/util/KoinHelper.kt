/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import ch.abwesend.privatecontacts.domain.lib.coroutine.ApplicationScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

object KoinHelper : KoinComponent

inline fun <reified T : Any> injectAnywhere(): Lazy<T> =
    KoinHelper.inject()

inline fun <reified T : Any> getAnywhere(): T =
    KoinHelper.get()

inline fun <reified T : Any> getAnywhereWithParams(vararg params: Any): T =
    KoinHelper.get { parametersOf(parameters = params) }

val applicationScope: ApplicationScope
    get() = getAnywhere()
