/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import android.net.Uri
import ch.abwesend.privatecontacts.domain.model.importexport.BinaryFileContent
import ch.abwesend.privatecontacts.domain.model.importexport.TextFileContent
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult

interface IFileAccessRepository {
    suspend fun readTextFileContent(fileUri: Uri, requestPermission: Boolean = true): FileReadResult
    suspend fun readBinaryFileContent(fileUri: Uri, requestPermission: Boolean = true): BinaryFileReadResult
    suspend fun writeFile(fileContent: TextFileContent, file: Uri, requestPermission: Boolean = true): FileWriteResult
}

typealias BinaryFileReadResult = BinaryResult<BinaryFileContent, Exception>
typealias FileReadResult = BinaryResult<TextFileContent, Exception>
typealias FileWriteResult = BinaryResult<Unit, Exception>
