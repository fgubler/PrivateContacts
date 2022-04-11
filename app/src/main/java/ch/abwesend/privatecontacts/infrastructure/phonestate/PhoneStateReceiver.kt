/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.phonestate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.model.contact.getFullName
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.arePhoneNumbersEquivalent
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

private const val CONSIDER_LAST_DIGITS = 4 // trade-off between speed and danger of missing a contact
private const val CONSIDER_MATCHING_CONTACTS = 5 // showing more does not make much sense

// TODO also offer using CallScreeningService
class PhoneStateReceiver : BroadcastReceiver() {
    private val contactRepository: IContactRepository by injectAnywhere()

    override fun onReceive(context: Context, intent: Intent?) {
        logger.debug("Receiving broadcast")

        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            // either no intent or an invalid action
            logger.debug("Ignoring invalid broadcast $intent")
            return
        }

        val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        logger.debug("Received phone state $phoneState")

        if (phoneState == TelephonyManager.EXTRA_STATE_RINGING) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            logger.debug("Receiving a call from $incomingNumber")

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val defaultCountryIso = telephonyManager?.networkCountryIso

            incomingNumber?.let { handleIncomingCall(it, defaultCountryIso) }
        }
    }

    private fun handleIncomingCall(phoneNumber: String, defaultCountryIso: String?) {
        applicationScope.launch {
            val correspondingContacts = findCorrespondingContacts(phoneNumber, defaultCountryIso)
            logger.debug("Found corresponding contacts: ${correspondingContacts.map { it.getFullName() }}")
        }
    }

    /**
     * Could be several (multiple contacts living together have the same number...)
     */
    private suspend fun findCorrespondingContacts(
        phoneNumber: String,
        defaultCountryIso: String?,
    ): List<ContactWithPhoneNumbers> {
        val ending = phoneNumber.takeLast(CONSIDER_LAST_DIGITS)
        val contactCandidates = contactRepository.findContactsWithNumberEndingOn(ending)

        return contactCandidates
            .filter { contact ->
                contact.phoneNumbers.any {
                    arePhoneNumbersEquivalent(
                        phoneNumber1 = phoneNumber,
                        phoneNumber2 = it.value,
                        defaultCountryIsoCode = defaultCountryIso,
                    )
                }
            }
            .take(CONSIDER_MATCHING_CONTACTS)
            .sortedBy { it.getFullName() }
    }
}
