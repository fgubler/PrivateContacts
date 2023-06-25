/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.filepicker

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ch.abwesend.privatecontacts.view.components.inputs.helper.CreateFileContract

private typealias CreateFileBaseLauncher = ManagedActivityResultLauncher<String, Uri?>

class CreateFileFilePickerLauncher private constructor(
    private val launcher: CreateFileBaseLauncher,
    private val defaultFilename: String,
) {
    fun launch() {
        launcher.launch(defaultFilename)
    }

    companion object {
        @Composable
        fun rememberLauncher(
            mimeType: String,
            defaultFilename: String,
            onFileSelected: (Uri?) -> Unit,
        ): CreateFileFilePickerLauncher {
            val launcher = rememberLauncherForActivityResult(
                contract = CreateFileContract(mimeType),
                onResult = onFileSelected,
            )
            return remember(mimeType) { CreateFileFilePickerLauncher(launcher, defaultFilename) }
        }
    }
}
