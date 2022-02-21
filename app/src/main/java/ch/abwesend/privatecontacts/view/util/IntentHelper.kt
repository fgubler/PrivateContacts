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

/**
 * It seems, a try-catch is better than checking beforehand because
 * there are some data-protection rules which might interfere.
 * See also https://developer.android.com/training/package-visibility
 */
fun Intent.tryStartActivity(context: Context) {
    logger.debug("Trying to start intent to '$data'")

    try {
        ContextCompat.startActivity(context, this, null)
    } catch(t: Throwable) {
        logger.warning("No app found for schema '${data?.scheme}'", t)
        Toast
            .makeText(context, R.string.deeplink_failed_no_appropriate_app, Toast.LENGTH_SHORT)
            .show()
    }
}