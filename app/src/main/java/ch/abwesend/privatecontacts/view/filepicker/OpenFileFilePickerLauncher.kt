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
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.components.inputs.helper.OpenFileContract

private typealias OpenFileBaseLauncher = ManagedActivityResultLauncher<Array<String>, Uri?>

class OpenFileFilePickerLauncher private constructor(
    private val launcher: OpenFileBaseLauncher,
    private val mimeTypes: Array<String>,
) {
    /** @return false if the launch failed */
    fun launch(): Boolean = try {
        launcher.launch(mimeTypes)
        true
    } catch (e: Exception) {
        logger.error("Failed to launch file-picker to open file", e)
        false
    }

    companion object {
        @Composable
        fun rememberOpenFileLauncher(
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
