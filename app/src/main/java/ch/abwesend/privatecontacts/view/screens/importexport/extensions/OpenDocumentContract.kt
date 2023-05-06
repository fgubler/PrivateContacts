/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport.extensions

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

/**
 * To make sure that we can actually read the document.
 * Not sure whether it is actually necessary, but...
 * See https://stackoverflow.com/questions/69092584/how-do-i-grant-permission-correctly-to-open-files-obtained-through-opendocument
 */
class OpenDocumentContract : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
