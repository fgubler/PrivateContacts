package ch.abwesend.privatecontacts.domain.service

import android.net.Uri
import ch.abwesend.privatecontacts.domain.repository.BinaryFileReadResult
import ch.abwesend.privatecontacts.domain.repository.IFileAccessRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class ContactImageService {
    private val fileRepository: IFileAccessRepository by injectAnywhere()

    suspend fun loadImageFromUri(uri: Uri): BinaryFileReadResult {
        return fileRepository.readBinaryFileContent(uri)
    }
}
