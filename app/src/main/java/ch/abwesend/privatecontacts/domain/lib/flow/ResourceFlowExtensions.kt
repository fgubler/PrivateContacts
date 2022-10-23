/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

typealias ResourceFlow<T> = Flow<AsyncResource<T>>
typealias ResourceFlowCollector<T> = FlowCollector<AsyncResource<T>>
typealias ResourceStateFlow<T> = StateFlow<AsyncResource<T>>
typealias MutableResourceStateFlow<T> = MutableStateFlow<AsyncResource<T>>

fun <T> mutableResourceStateFlow(
    initialValue: AsyncResource<T> = InactiveResource()
): MutableResourceStateFlow<T> = MutableStateFlow(initialValue)

suspend fun <T> ResourceFlowCollector<T>.emitLoading() = emit(LoadingResource())
suspend fun <T> ResourceFlowCollector<T>.emitReady(value: T) = emit(ReadyResource(value))
suspend fun <T> ResourceFlowCollector<T>.emitError(error: Throwable) = emit(ErrorResource(listOf(error)))
suspend fun <T> ResourceFlowCollector<T>.emitInactive() = emit(InactiveResource())

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

fun <TThis, TOther, TResult> ResourceFlow<TThis>.combineResource(
    other: ResourceFlow<TOther>,
    mapper: (TThis, TOther) -> TResult,
) = combine(other) { thisResource, otherResource -> thisResource.combineWith(otherResource, mapper) }

@Suppress("USELESS_CAST") // it is actually needed
fun <T> Flow<T>.toResourceFlow(): ResourceFlow<T> = flow {
    try {
        emitLoading()

        val innerFlow: Flow<AsyncResource<T>> = map { ReadyResource(it) as AsyncResource<T> }
            .catch { t ->
                logger.error("Error in inner flow while transforming to ResourceStateFlow", t)
                emit(ErrorResource(listOf(t)))
            }

        emitAll(innerFlow)
    } catch (e: Exception) {
        emitError(e)
        logger.error("Failed to transform Flow to ResourceFlow", e)
    }
}

fun <T, S> ResourceFlow<T>.mapReady(mapper: (T) -> S): ResourceFlow<S> =
    map { resource -> resource.mapReady(mapper) }