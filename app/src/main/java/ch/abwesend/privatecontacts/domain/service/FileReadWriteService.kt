/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.importexport.TextFileContent
import ch.abwesend.privatecontacts.domain.repository.FileReadResult
import ch.abwesend.privatecontacts.domain.repository.FileWriteResult
import ch.abwesend.privatecontacts.domain.repository.IFileAccessRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class FileReadWriteService {
    private val repository: IFileAccessRepository by injectAnywhere()

    suspend fun readFileContent(fileUri: Uri): FileReadResult =
        repository.readTextFileContent(fileUri)

    suspend fun writeContentToFile(content: TextFileContent, fileUri: Uri): FileWriteResult =
        repository.writeFile(content, fileUri)
}
