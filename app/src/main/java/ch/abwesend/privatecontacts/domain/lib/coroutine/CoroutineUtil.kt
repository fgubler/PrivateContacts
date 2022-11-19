package ch.abwesend.privatecontacts.domain.lib.coroutine

import ch.abwesend.privatecontacts.domain.util.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** runs the [mapper] over each element in parallel */
suspend fun <T, S> Collection<T>.mapAsync(mapper: suspend (T) -> S): List<S> = coroutineScope {
    val deferred = map { async { mapper(it) } }
    deferred.awaitAll()
}

/** same as [mapAsync] but chunked */
suspend fun <T, S> Collection<T>.mapAsyncChunked(
    chunkSize: Int = Constants.defaultChunkSize,
    mapper: suspend (T) -> S
): List<S> = coroutineScope {
    chunked(chunkSize).flatMap { chunk ->
        chunk.mapAsync(mapper)
    }
}
