package ch.abwesend.privatecontacts.domain.util

val Any.simpleClassName: String
    get() = this::class.java.simpleName
