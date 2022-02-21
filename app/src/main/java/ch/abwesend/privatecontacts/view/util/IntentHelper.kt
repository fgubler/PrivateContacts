/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger

fun Intent.tryStartActivity(context: Context) {
    logger.debug("Trying to start intent to '$data'")
    if (resolveActivity(context.packageManager) != null) {
        ContextCompat.startActivity(context, this, null)
    } else {
        Toast
            .makeText(context, R.string.deeplink_failed_no_appropriate_app, Toast.LENGTH_SHORT)
            .show()
    }
}