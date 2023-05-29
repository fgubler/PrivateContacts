/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

interface IFileAccessRepository {
    suspend fun readFileContent(fileUri: Uri, requestPermission: Boolean = true): FileReadResult
    suspend fun writeFile(file: Uri, fileContent: FileContent, requestPermission: Boolean = true): FileWriteResult
}

typealias FileReadResult = BinaryResult<FileContent, Exception>
typealias FileWriteResult = BinaryResult<Unit, Exception>
