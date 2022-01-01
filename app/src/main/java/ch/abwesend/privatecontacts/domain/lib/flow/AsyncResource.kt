package ch.abwesend.privatecontacts.domain.lib.flow

sealed class AsyncResourceGeneric<TValue, TError> {
    abstract val valueOrNull: TValue?

    open fun ifReady(handler: (TValue) -> Unit) {}
    open fun ifLoading(handler: () -> Unit) {}
    open fun ifError(handler: (List<TError>) -> Unit) {}
    open fun ifInactive(handler: () -> Unit) {}
}

typealias AsyncResource<TValue> = AsyncResourceGeneric<TValue, Exception>

data class ReadyResource<T>(val value: T) : AsyncResource<T>() {
    override val valueOrNull = value
    override fun ifReady(handler: (T) -> Unit): Unit = handler(value)
}

class LoadingResource<T> : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifLoading(handler: () -> Unit): Unit = handler()
}

data class ErrorResource<T>(val error: List<Exception>) : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifError(handler: (List<Exception>) -> Unit): Unit = handler(error)
}

class InactiveResource<T> : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifInactive(handler: () -> Unit): Unit = handler()
}
