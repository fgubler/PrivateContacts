/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactData
import ch.abwesend.privatecontacts.domain.model.contactdata.StringBasedContactDataSimple
import ch.abwesend.privatecontacts.domain.model.contactdata.Website
import ch.abwesend.privatecontacts.view.screens.contactdetail.UTF_8
import java.net.URLEncoder

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

fun PhoneNumber.navigateToSms(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", value, null))
        intent.tryStartActivity(context)
    } catch(e: Exception) {
        logger.error("Failed to send intent for SMS", e)
    }
}

fun PhoneNumber.navigateToDial(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", value, null))
        intent.tryStartActivity(context)
    } catch(e: Exception) {
        logger.error("Failed to send intent for Call", e)
    }
}

fun EmailAddress.navigateToEmailClient(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("mailto", value, null))
        intent.tryStartActivity(context)
    } catch(e: Exception) {
        logger.error("Failed to send intent for Email", e)
    }
}

fun PhysicalAddress.navigateToLocation(context: Context) {
    try {
        val locationEncoded = URLEncoder.encode(value, UTF_8)
        val uri = "geo:0,0?q=$locationEncoded"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.tryStartActivity(context)
    } catch(e: Exception) {
        logger.error("Failed to send intent for Location", e)
    }
}

fun Website.navigateToBrowser(context: Context) {
    try {
        val url = value.takeIf {
            value.startsWith("http") || value.startsWith("https")
        } ?: "http://$value"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.tryStartActivity(context)
    } catch(e: Exception) {
        logger.error("Failed to send intent for Website", e)
    }
}

fun StringBasedContactDataSimple.navigateToOnlineSearch(context: Context) {
    try {
        val companyName = URLEncoder.encode(value, UTF_8)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://duckduckgo.com/?q=${companyName}"))
        intent.tryStartActivity(context)
    } catch(e: Exception) {
        logger.error("Failed to send intent for ${javaClass.simpleName}", e)
    }
}