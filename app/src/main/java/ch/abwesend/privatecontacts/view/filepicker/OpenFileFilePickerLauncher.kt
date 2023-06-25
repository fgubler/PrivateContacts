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
import ch.abwesend.privatecontacts.view.components.inputs.helper.OpenFileContract

private typealias BaseLauncher = ManagedActivityResultLauncher<Array<String>, Uri?>

class OpenFileFilePickerLauncher private constructor(
    private val launcher: BaseLauncher,
    private val mimeTypes: Array<String>,
) {
    fun launch() {
        launcher.launch(mimeTypes)
    }

    companion object {
        @Composable
        fun rememberLauncher(
            mimeTypes: Array<String>,
            onFileSelected: (Uri?) -> Unit,
        ): OpenFileFilePickerLauncher {
            val launcher = rememberLauncherForActivityResult(
                contract = OpenFileContract(),
                onResult = onFileSelected,
            )
            return remember(mimeTypes) { OpenFileFilePickerLauncher(launcher, mimeTypes) }
        }
    }
}
