package ch.abwesend.privatecontacts.domain.util

val Any.simpleClassName: String
    get() = this::class.java.simpleName

/**
 * Allows an if-like functionality in the context of method-chaining.
 */
fun <T> T.doIf(condition: Boolean, doIfTrue: (T) -> T): T =
    if (condition) doIfTrue(this)
    else this
