package ch.abwesend.privatecontacts.view.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.getAnywhere

// TODO return ENUM
fun PhoneNumber.navigateToWhatsApp(context: Context): Boolean = try {
    // TODO check if this actually works
    tryGetInternationalFormat()?.let { number ->
        val url = "https://api.whatsapp.com/send?phone=$number"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        context.startActivity(intent)
        true
    } ?: false.also { logger.info("Number is not in international format") }
} catch (e: Exception) {
    logger.error("Failed to send intent to whatsapp", e)
    false
}

private fun PhoneNumber.tryGetInternationalFormat(): String? {
    val telephoneService = getAnywhere<TelephoneService>() // TODO solve more cleanly
    return value.takeIf { telephoneService.isInInternationalFormat(it) }
        ?: formattedValue.takeIf { telephoneService.isInInternationalFormat(it) }
            ?.let { phoneNumber -> telephoneService.removeSeparators(phoneNumber) }
}
