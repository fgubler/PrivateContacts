/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactWithPhoneNumbers
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

private const val CONSIDER_LAST_DIGITS = 4 // trade-off between speed and danger of missing a contact
private const val CONSIDER_MATCHING_CONTACTS = 5 // showing more does not make much sense

class IncomingCallService {
    private val contactRepository: IContactRepository by injectAnywhere()
    private val telephoneService: TelephoneService by injectAnywhere()

    /**
     * Load contacts corresponding to the given [phoneNumber].
     * Could be several (multiple contacts living together have the same number...)
     */
    suspend fun findCorrespondingContacts(phoneNumber: String): List<ContactWithPhoneNumbers> {
        val ending = phoneNumber.takeLast(CONSIDER_LAST_DIGITS)
        val contactCandidates = contactRepository.findContactsWithNumberEndingOn(ending)

        logger.debug("Found ${contactCandidates.size} candidate(s) for calling phone-number")

        val matchingCandidates = filterCandidates(phoneNumber, contactCandidates).ifEmpty {
            filterCandidates(phoneNumber, contactCandidates, forceLegacyComparison = true)
        }

        return matchingCandidates
            .take(CONSIDER_MATCHING_CONTACTS)
            .sortedBy { it.displayName }
    }

    private fun filterCandidates(
        phoneNumber: String,
        candidates: List<ContactWithPhoneNumbers>,
        forceLegacyComparison: Boolean = false,
    ) =
        candidates.filter { contact ->
            contact.phoneNumbers.any {
                telephoneService.arePhoneNumbersEquivalent(
                    phoneNumber1 = phoneNumber,
                    phoneNumber2 = it.value,
                    forceLegacyComparison = forceLegacyComparison,
                )
            }
        }
}
