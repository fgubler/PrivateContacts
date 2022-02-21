/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

sealed class AsyncResourceGeneric<TValue, TError> {
    abstract val valueOrNull: TValue?

    open fun ifReady(handler: ReadyHandler<TValue>) {}
    open fun ifLoading(handler: LoadingHandler) {}
    open fun ifError(handler: ErrorHandlerGeneric<TError>) {}
    open fun ifInactive(handler: InactiveHandler) {}
}

typealias AsyncResource<TValue> = AsyncResourceGeneric<TValue, Exception>

data class ReadyResource<T>(val value: T) : AsyncResource<T>() {
    override val valueOrNull = value
    override fun ifReady(handler: ReadyHandler<T>): Unit = handler(value)
}

class LoadingResource<T> : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifLoading(handler: LoadingHandler): Unit = handler()
}

data class ErrorResource<T>(val errors: List<Exception>) : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifError(handler: ErrorHandlerGeneric<Exception>): Unit = handler(errors)
}

class InactiveResource<T> : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifInactive(handler: InactiveHandler): Unit = handler()
}

fun interface ReadyHandler<T> {
    operator fun invoke(value: T)
}
fun interface LoadingHandler {
    operator fun invoke()
}
fun interface InactiveHandler {
    operator fun invoke()
}
fun interface ErrorHandlerGeneric<TError> {
    operator fun invoke(errors: List<TError>)
}
typealias ErrorHandler = ErrorHandlerGeneric<Exception>
