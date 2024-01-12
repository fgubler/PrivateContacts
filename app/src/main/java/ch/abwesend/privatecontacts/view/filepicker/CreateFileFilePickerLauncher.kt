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
import ch.abwesend.privatecontacts.view.components.inputs.helper.CreateFileContract

private typealias CreateFileBaseLauncher = ManagedActivityResultLauncher<String, Uri?>

class CreateFileFilePickerLauncher private constructor(
    private val launcher: CreateFileBaseLauncher,
    private val defaultFilename: String,
) {
    fun launch(): Boolean = try {
        launcher.launch(defaultFilename)
        true
    } catch (e: Exception) {
        logger.error("Failed to launch file-picker to open file", e)
        false
    }

    companion object {
        @Composable
        fun rememberCreateFileLauncher(
            mimeType: String,
            defaultFilename: String,
            onFileSelected: (Uri?) -> Unit,
        ): CreateFileFilePickerLauncher {
            val launcher = rememberLauncherForActivityResult(
                contract = CreateFileContract(mimeType),
                onResult = onFileSelected,
            )
            return remember(mimeType, defaultFilename) {
                CreateFileFilePickerLauncher(launcher, defaultFilename)
            }
        }
    }
}
