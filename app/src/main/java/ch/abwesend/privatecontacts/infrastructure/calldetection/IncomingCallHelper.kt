/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.content.Context
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.service.IncomingCallService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.ToastRepository
import kotlinx.coroutines.launch

class IncomingCallHelper {
    private val incomingCallService: IncomingCallService by injectAnywhere()
    private val telephoneService: TelephoneService by injectAnywhere()
    private val notificationRepository: CallNotificationRepository by injectAnywhere()
    private val toastRepository: ToastRepository by injectAnywhere()

    suspend fun matchesAnyKnownContact(phoneNumber: String): Boolean {
        val matchingContacts = incomingCallService.findCorrespondingContacts(
            phoneNumber = phoneNumber,
            considerPublicContacts = true,
        )
        logger.debug("Found ${matchingContacts.size} matching contacts")
        return matchingContacts.isNotEmpty()
    }

    suspend fun handleIncomingCall(
        context: Context,
        phoneNumber: String,
    ) {
        val correspondingContacts = incomingCallService
            .findCorrespondingContacts(phoneNumber)
            .map { it.displayName }
            .distinct()

        logger.debug("Found corresponding contacts: $correspondingContacts")
        val formattedNumber = telephoneService.formatPhoneNumberForDisplay(phoneNumber)
        val notificationText = createNotificationText(context, formattedNumber, correspondingContacts)

        notificationText?.let { text ->
            notificationRepository.showIncomingCallNotification(context, text)
            toastRepository.showToastNotification(context, text)
        } ?: logger.debug("No notification text: don't show notification")
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

    fun handleBlockedCall(
        context: Context,
        phoneNumber: String,
    ) {
        applicationScope.launch {
            val formattedNumber = telephoneService.formatPhoneNumberForDisplay(phoneNumber)
            val notificationText = context.getString(R.string.blocked_call_notification_text, formattedNumber)

            logger.debugLocally("Showing notification for blocked call from $formattedNumber")
            notificationRepository.showIncomingCallNotification(context, notificationText)
        }
    }
}
