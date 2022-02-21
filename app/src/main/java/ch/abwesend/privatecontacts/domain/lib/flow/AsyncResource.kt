/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

sealed class AsyncResourceGeneric<TValue, TError> {
    abstract val valueOrNull: TValue?

    open fun ifReady(handler: ReadyHandler<TValue>): AsyncResourceGeneric<TValue, TError> = this
    open fun ifLoading(handler: LoadingHandler): AsyncResourceGeneric<TValue, TError> = this
    open fun ifError(handler: ErrorHandlerGeneric<TError>): AsyncResourceGeneric<TValue, TError> = this
    open fun ifInactive(handler: InactiveHandler): AsyncResourceGeneric<TValue, TError> = this
}

typealias AsyncResource<TValue> = AsyncResourceGeneric<TValue, Exception>

data class ReadyResource<T>(val value: T) : AsyncResource<T>() {
    override val valueOrNull = value
    override fun ifReady(handler: ReadyHandler<T>): ReadyResource<T> {
        handler(value)
        return this
    }
}

class LoadingResource<T> : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifLoading(handler: LoadingHandler): LoadingResource<T> {
        handler()
        return this
    }
}

data class ErrorResource<T>(val errors: List<Exception>) : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifError(handler: ErrorHandlerGeneric<Exception>): ErrorResource<T> {
        handler(errors)
        return this
    }
}

class InactiveResource<T> : AsyncResource<T>() {
    override val valueOrNull: T? = null
    override fun ifInactive(handler: InactiveHandler): InactiveResource<T> {
        handler()
        return this
    }
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
