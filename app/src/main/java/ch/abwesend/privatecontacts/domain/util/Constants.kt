/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import java.io.File

object Constants {
    val linebreak: String by lazy {
        System.getProperty("line.separator") ?: "\n"
    }

    val doubleLinebreak: String
        get() = linebreak + linebreak

    val filePathSeparator: String
        get() = File.separator

    /** chunk-size for "expensive" operations run in parallel; e.g. database access) */
    const val defaultChunkSize: Int = 100
}
