/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.inputs.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

/**
 * To make sure that we can actually read the document.
 * Not sure whether it is actually necessary, but...
 * See https://stackoverflow.com/questions/69092584/how-do-i-grant-permission-correctly-to-open-files-obtained-through-opendocument
 */
class OpenFileContract(private val readOnly: Boolean = true) : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input).apply {
            addFlags(getFlagsForFile(readOnly))
        }
    }
}

/**
 * Same das [OpenFileContract] just for creating a file
 */
class CreateFileContract(mimeType: String) : ActivityResultContracts.CreateDocument(mimeType) {
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input).apply {
            addFlags(getFlagsForFile(readOnly = false))
        }
    }
}

/**
 * Same das [OpenFileContract] just for opening a folder
 */
class OpenFolderContract(private val readOnly: Boolean = true) : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return super.createIntent(context, input).apply {
            addFlags(getFlagsForFile(readOnly))
        }
    }
}

private fun getFlagsForFile(readOnly: Boolean): Int =
    (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION).let {
        if (readOnly) it else it or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
