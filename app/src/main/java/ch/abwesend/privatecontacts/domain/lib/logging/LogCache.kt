/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.logging

import ch.abwesend.privatecontacts.domain.util.Constants
import java.util.LinkedList

const val MAX_LENGTH = 1000
object LogCache {
    private val latestLogs: MutableList<Pair<LogLevel, String>> = LinkedList()

    @Synchronized
    fun tryAddLog(level: LogLevel, message: String): Boolean =
        runCatching {
            latestLogs.add(level to message)
            while (latestLogs.size > MAX_LENGTH) {
                latestLogs.removeFirstOrNull()
            }
            true
        }.getOrNull() ?: false

    fun getLog(): String = latestLogs.joinToString(Constants.linebreak) { (level, message) ->
        "${level.name}: $message"
    }
}
