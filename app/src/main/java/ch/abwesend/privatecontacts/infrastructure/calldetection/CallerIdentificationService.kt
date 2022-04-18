/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.telecom.Call
import android.telecom.CallScreeningService
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class CallerIdentificationService : CallScreeningService() {
    private val incomingCallHelper: IncomingCallHelper by injectAnywhere()

    override fun onScreenCall(callDetails: Call.Details) {
        val number: String = callDetails.handle.schemeSpecificPart
        logger.debug("Incoming call from $number")
        incomingCallHelper.handleIncomingCall(baseContext, number)
    }
}
