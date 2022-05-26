/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class CallerIdentificationService : CallScreeningService() {
    private val incomingCallHelper: IncomingCallHelper by injectAnywhere()

    override fun onScreenCall(callDetails: Call.Details) {
        logger.debug("Receiving incoming call")
        val response = CallResponse.Builder().build() // we don't want to interfere with anything here...
        respondToCall(callDetails, response)

        if (shouldHandleCall(callDetails)) {
            val number: String = callDetails.handle.schemeSpecificPart
            logger.debug("Incoming call from $number")
            incomingCallHelper.handleIncomingCall(baseContext, number)
        } else logger.debug("Ignored not-incoming call")
    }

    private fun shouldHandleCall(callDetails: Call.Details): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        } else true
    }
}
