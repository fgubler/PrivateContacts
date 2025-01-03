/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

import ch.abwesend.privatecontacts.domain.util.Constants

@JvmInline
value class TextFileContent(val content: String) : CharSequence by content {
    val numberOfLines: Int
        get() = content.count { it.toString() == Constants.linebreak }
}

@JvmInline
value class BinaryFileContent(val content: ByteArray)
