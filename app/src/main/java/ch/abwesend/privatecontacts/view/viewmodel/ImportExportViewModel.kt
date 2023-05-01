/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ch.abwesend.privatecontacts.domain.service.FilePickerSanitizingService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import java.io.File

class ImportExportViewModel : ViewModel() {
    private val sanitizingService: FilePickerSanitizingService by injectAnywhere()

    private val _importFile: MutableState<File?> = mutableStateOf(null)
    val importFile: State<File?> = _importFile

    fun getSanitizedFileOrNull(uri: Uri): File? =
        uri.path?.let { sanitizingService.getValidFileOrNull(it) }

    fun setImportFile(path: File) {
        _importFile.value = path
    }
}
