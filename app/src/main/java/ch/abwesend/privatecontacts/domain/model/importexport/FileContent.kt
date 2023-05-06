/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.importexport

@JvmInline
value class FileContent(private val lines: List<String>) : List<String> by lines
