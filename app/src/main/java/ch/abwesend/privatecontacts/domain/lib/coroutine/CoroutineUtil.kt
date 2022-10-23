package ch.abwesend.privatecontacts.domain.lib.coroutine

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * runs the [mapper] over each element in parallel
 */
suspend fun <T, S> Collection<T>.mapAsync(mapper: suspend (T) -> S): List<S> = coroutineScope {
    val deferred = map { async { mapper(it) } }
    deferred.awaitAll()
}
