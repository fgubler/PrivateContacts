package ch.abwesend.privatecontacts.view.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.view.model.whatsapp.WhatsAppNavigationResult
import ch.abwesend.privatecontacts.view.model.whatsapp.WhatsAppNavigationResult.NAVIGATION_FAILED
import ch.abwesend.privatecontacts.view.model.whatsapp.WhatsAppNavigationResult.NOT_INSTALLED
import ch.abwesend.privatecontacts.view.model.whatsapp.WhatsAppNavigationResult.PHONE_NUMBER_INVALID_FORMAT
import ch.abwesend.privatecontacts.view.model.whatsapp.WhatsAppNavigationResult.SUCCESS

private const val NUMBER_OF_NAVIGATION_TYPES = 3

fun PhoneNumber.tryNavigateToWhatsApp(context: Context, clickCounter: Int): WhatsAppNavigationResult = try {
    if (!isWhatsAppInstalled()) {
        NOT_INSTALLED
    } else {
        val result = navigateToWhatsApp(context, clickCounter)
        when (result) {
            NOT_INSTALLED, PHONE_NUMBER_INVALID_FORMAT, SUCCESS -> result
            NAVIGATION_FAILED -> {
                val retry = clickCounter < NUMBER_OF_NAVIGATION_TYPES
                if (retry) tryNavigateToWhatsApp(context, clickCounter + 1)
                else result
            }
        }
    }
} catch (e: Exception) {
    logger.warning("Failed to send intent to whatsapp with click-counter $clickCounter", e)
    NAVIGATION_FAILED
}

private fun isWhatsAppInstalled(): Boolean {
    return true // TODO implement
}

private fun PhoneNumber.navigateToWhatsApp(context: Context, clickCounter: Int): WhatsAppNavigationResult =
    tryGetInternationalFormat()?.let { phoneNumber ->
        val uri = createIntentUri(phoneNumber, clickCounter)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(uri)
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
        SUCCESS
    } ?: PHONE_NUMBER_INVALID_FORMAT

private fun createIntentUri(phoneNumber: String, clickCounter: Int): Uri {
    val whatsAppUrl: String = when (clickCounter % NUMBER_OF_NAVIGATION_TYPES) {
        0 -> "https://api.whatsapp.com/send?phone=$phoneNumber"
        1 -> "whatsapp://send?phone=$phoneNumber"
        2 -> "https://wa.me/$phoneNumber"
        else -> createIntentUri(phoneNumber, 0).toString() // cannot happen
    }

    return Uri.parse(whatsAppUrl)
}

private fun PhoneNumber.tryGetInternationalFormat(): String? {
    val telephoneService = getAnywhere<TelephoneService>() // TODO solve more cleanly
    return value.takeIf { telephoneService.isInInternationalFormat(it) }
        ?: formattedValue.takeIf { telephoneService.isInInternationalFormat(it) }
            ?.let { phoneNumber -> telephoneService.removeSeparators(phoneNumber) }
}
