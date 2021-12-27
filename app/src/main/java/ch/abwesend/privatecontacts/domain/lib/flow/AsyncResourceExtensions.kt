package ch.abwesend.privatecontacts.domain.lib.flow

import androidx.lifecycle.LifecycleOwner
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

fun <T> ResourceStateFlow<T>.observeLoading(owner: LifecycleOwner, observer: () -> Unit, ): ResourceStateFlow<T> {
    observe(owner) { it.ifLoading(observer) }
    return this
}

fun <T> ResourceStateFlow<T>.observeReady(owner: LifecycleOwner, observer: (T) -> Unit): ResourceStateFlow<T> {
    observe(owner) { it.ifReady(observer) }
    return this
}

fun <T> ResourceStateFlow<T>.observeError(owner: LifecycleOwner, observer: (List<Exception>) -> Unit):
    ResourceStateFlow<T> {
    observe(owner) { it.ifError(observer) }
    return this
}

fun <T> ResourceStateFlow<T>.observeInactive(owner: LifecycleOwner, observer: () -> Unit):
    ResourceStateFlow<T> {
    observe(owner) { it.ifInactive(observer) }
    return this
}
