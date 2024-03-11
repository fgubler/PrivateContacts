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
    // TODO check if this actually works: works
    tryGetInternationalFormat()?.let { number ->
        val url = "https://api.whatsapp.com/send?phone=$number"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
        true
    } ?: false.also { logger.info("Number is not in international format") }
} catch (e: Exception) {
    logger.error("Failed to send intent to whatsapp", e)
    false
}
fun PhoneNumber.navigateToWhatsApp2(context: Context): Boolean = try {
    // TODO check if this actually works: works
    tryGetInternationalFormat()?.let { number ->
        val url = "whatsapp://send?phone=$number"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
        true
    } ?: false.also { logger.info("Number is not in international format") }
} catch (e: Exception) {
    logger.error("Failed to send intent to whatsapp", e)
    false
}
fun PhoneNumber.navigateToWhatsApp3(context: Context): Boolean = try {
    // TODO check if this actually works: does NOT work
    tryGetInternationalFormat()?.let { number ->
        val intent = Intent(Intent.ACTION_VIEW).also {
            it.action = Intent.ACTION_SEND
            it.setPackage("com.whatsapp")
            it.putExtra(Intent.EXTRA_PHONE_NUMBER, number)
        }
        context.startActivity(intent)
        true
    } ?: false.also { logger.info("Number is not in international format") }
} catch (e: Exception) {
    logger.error("Failed to send intent to whatsapp", e)
    false
}

fun PhoneNumber.navigateToWhatsApp4(context: Context): Boolean = try {
    // TODO check if this actually works: works
    tryGetInternationalFormat()?.let { number ->
        val url = "https://wa.me/$number"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        intent.setPackage("com.whatsapp")
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
