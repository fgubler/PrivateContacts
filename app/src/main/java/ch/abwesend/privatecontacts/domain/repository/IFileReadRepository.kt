/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.result.BinaryResult

interface IFileReadRepository {
    suspend fun readFileContent(fileUri: Uri, requestPermission: Boolean = true): FileReadResult
}

typealias FileReadResult = BinaryResult<List<String>, Exception>
