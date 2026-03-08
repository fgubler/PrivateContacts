/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.filepicker

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.components.inputs.helper.OpenFolderContract

private typealias OpenFolderBaseLauncher = ManagedActivityResultLauncher<Uri?, Uri?>

class OpenFolderPickerLauncher private constructor(
    private val launcher: OpenFolderBaseLauncher,
) {
    fun launch(): Boolean = try {
        launcher.launch(null)
        true
    } catch (e: Exception) {
        logger.error("Failed to launch folder-picker", e)
        false
    }

    companion object {
        @Composable
        fun rememberOpenFolderLauncher(
            onFolderSelected: (Uri?) -> Unit,
        ): OpenFolderPickerLauncher {
            val launcher = rememberLauncherForActivityResult(
                contract = OpenFolderContract(readOnly = false),
                onResult = onFolderSelected,
            )
            return remember {
                OpenFolderPickerLauncher(launcher)
            }
        }
    }
}
