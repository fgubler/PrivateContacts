/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.content.Context
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.service.IncomingCallService
import ch.abwesend.privatecontacts.domain.service.interfaces.ITelephoneService
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class IncomingCallHelper {
    private val incomingCallService: IncomingCallService by injectAnywhere()
    private val telephoneService: ITelephoneService by injectAnywhere()
    private val notificationRepository: NotificationRepository by injectAnywhere()
    private val toastRepository: ToastRepository by injectAnywhere()

    fun handleIncomingCall(
        context: Context,
        phoneNumber: String,
    ) {
        val defaultCountryIso = telephoneService.telephoneDefaultCountryIso

        applicationScope.launch {
            val correspondingContacts = incomingCallService
                .findCorrespondingContacts(phoneNumber, defaultCountryIso)
                .map { it.getFullName() }
                .distinct()

            logger.debug("Found corresponding contacts: $correspondingContacts")
            val formattedNumber = telephoneService.formatPhoneNumberForDisplay(phoneNumber)
            val notificationText = createNotificationText(context, formattedNumber, correspondingContacts)

            notificationText?.let { text ->
                notificationRepository.showIncomingCallNotification(context, text)
                toastRepository.showToastNotification(context, text)
            } ?: logger.debug("No notification text: don't show notification")
        }
    }

    private fun createNotificationText(
        context: Context,
        callerNumber: String,
        contactNames: List<String>,
    ): String? =
        if (contactNames.isEmpty()) null
        else if (contactNames.size == 1) {
            context.getString(R.string.incoming_call_text_single_match, contactNames.first())
        } else {
            val lastContactName = contactNames.last()
            val otherContactNames = contactNames
                .filter { it != lastContactName }
                .joinToString(separator = ", ")
            context.getString(
                R.string.incoming_call_text_multiple_matches,
                otherContactNames,
                lastContactName,
                callerNumber,
            )
        }
}
