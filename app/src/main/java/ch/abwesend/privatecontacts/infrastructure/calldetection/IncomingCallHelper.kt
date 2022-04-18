/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.content.Context
import android.telephony.PhoneNumberUtils
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.repository.ITelephoneRepository
import ch.abwesend.privatecontacts.domain.service.IncomingCallService
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class IncomingCallHelper {
    private val incomingCallService: IncomingCallService by injectAnywhere()
    private val telephoneService: ITelephoneRepository by injectAnywhere()
    private val notificationRepository: NotificationRepository by injectAnywhere()

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
            val formattedNumber = PhoneNumberUtils.formatNumber(phoneNumber, defaultCountryIso) ?: phoneNumber
            notificationRepository.showIncomingCallNotification(context, formattedNumber, correspondingContacts)
        }
    }
}
