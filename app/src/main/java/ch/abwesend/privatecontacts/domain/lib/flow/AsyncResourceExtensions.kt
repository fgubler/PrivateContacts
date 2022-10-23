/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

import ch.abwesend.privatecontacts.domain.lib.logging.logger

/** the lower the index, the higher the priority */
private val resourcePriorities = listOf(
    ErrorResource::class,
    LoadingResource::class,
    InactiveResource::class,
    ReadyResource::class, // ReadyResource should ALWAYS have the lowest priority
)

/**
 * Combines the two resources according to their priorities.
 * [mapperForReadyValues] is only applied if both resources are [ReadyResource].
 */
fun <TThis, TOther, TResult> AsyncResource<TThis>.combineWith(
    other: AsyncResource<TOther>,
    mapperForReadyValues: (TThis, TOther) -> TResult,
): AsyncResource<TResult> {
    val thisPriority = resourcePriorities.indexOf(this::class)
    val otherPriority = resourcePriorities.indexOf(other::class)
    val otherIsMoreImportant = otherPriority < thisPriority // low index means high priority

    return if (otherIsMoreImportant) {
        other.combineWith(this) { first, second -> mapperForReadyValues(second, first) }
    } else {
        when (this) {
            is ErrorResource -> {
                val otherErrors = (other as? ErrorResource)?.errors.orEmpty()
                ErrorResource(errors + otherErrors)
            }
            is LoadingResource -> LoadingResource()
            is InactiveResource -> InactiveResource()
            is ReadyResource -> {
                val otherValue = (other as? ReadyResource)?.value
                otherValue
                    ?.let { ReadyResource(mapperForReadyValues(value, otherValue)) }
                    ?: handleReadyResourceCombineError(other)
            }
        }
    }
}

private fun <S, T> handleReadyResourceCombineError(other: AsyncResource<T>): AsyncResource<S> {
    val error = IllegalStateException(
        "Invalid state: other should be ReadyResource but is ${other::class.java.simpleName} instead"
    ).also { other.logger.error(it) }
    return ErrorResource(listOf(error))
}

fun <T, S> AsyncResource<T>.mapReady(mapper: (T) -> S): AsyncResource<S> =
    when (this) {
        is ErrorResource -> ErrorResource(errors = errors)
        is InactiveResource -> InactiveResource()
        is LoadingResource -> LoadingResource()
        is ReadyResource -> ReadyResource(value = mapper(value))
    }
