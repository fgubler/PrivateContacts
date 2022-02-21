/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

typealias ResourceStateFlow<T> = StateFlow<AsyncResource<T>>
typealias MutableResourceStateFlow<T> = MutableStateFlow<AsyncResource<T>>

fun <T> mutableResourceStateFlow(
    initialValue: AsyncResource<T> = InactiveResource()
): MutableResourceStateFlow<T> = MutableStateFlow(initialValue)

suspend fun <T> MutableResourceStateFlow<T>.emitLoading() = emit(LoadingResource())
suspend fun <T> MutableResourceStateFlow<T>.emitReady(value: T) = emit(ReadyResource(value))
suspend fun <T> MutableResourceStateFlow<T>.emitError(error: Exception) = emit(ErrorResource(listOf(error)))
suspend fun <T> MutableResourceStateFlow<T>.emitInactive() = emit(InactiveResource())

suspend fun <T> MutableResourceStateFlow<T>.withLoadingState(loader: suspend () -> T): T? =
    try {
        emitLoading()
        val result = loader()
        emitReady(result)
        result
    } catch (e: Exception) {
        emitError(e)
        logger.error("Failed to load data", e)
        null
    }
