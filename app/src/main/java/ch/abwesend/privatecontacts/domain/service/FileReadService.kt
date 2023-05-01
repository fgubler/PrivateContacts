/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.repository.FileReadResult
import ch.abwesend.privatecontacts.domain.repository.IFileReadRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class FileReadService {
    val repository: IFileReadRepository by injectAnywhere()

    suspend fun readFileContent(fileUri: Uri): FileReadResult =
        repository.readFileContent(fileUri)
}
