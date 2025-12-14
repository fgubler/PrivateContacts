/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.content.Context
import android.os.Build
import android.telecom.Call.Details
import android.telecom.CallScreeningService
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import kotlinx.coroutines.launch

class CallerIdentificationService : CallScreeningService() {
    private val incomingCallHelper: IncomingCallHelper by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()
    private val context: Context = this

    override fun onScreenCall(callDetails: Details) {
        logger.debug("Receiving incoming call")

        if (shouldHandleCall(callDetails)) {
            handleIncomingCall(callDetails)
        } else {
            logger.debug("Ignored not-incoming call")
        }
    }

    private fun shouldHandleCall(callDetails: Details): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callDetails.callDirection == Details.DIRECTION_INCOMING
        } else true
    }

    private fun handleIncomingCall(callDetails: Details) = applicationScope.launch {
        val phoneNumber: String? = callDetails.handle?.schemeSpecificPart
        logger.debug("Incoming call from number '$phoneNumber'")

        val blockCallsFromUnknownNumbers = Settings.nextOrDefault().blockIncomingCallsFromUnknownNumbers
        val hasReadContactsPermission = permissionService.hasContactReadPermission()

        val shouldBlockCall = phoneNumber != null &&
            blockCallsFromUnknownNumbers &&
            hasReadContactsPermission &&
            !incomingCallHelper.matchesAnyKnownContact(phoneNumber)

        if (shouldBlockCall) {
            blockCall(callDetails, phoneNumber)
        } else {
            val response = CallResponse.Builder().build() // we don't want to interfere with anything here...
            respondToCall(callDetails, response)
            phoneNumber?.let { incomingCallHelper.handleIncomingCall(baseContext, it) }
        }
    }

    private fun blockCall(callDetails: Details, phoneNumber: String?) {
        logger.info("Blocking incoming call from unknown number")
        logger.debugLocally("Blocking call with number $phoneNumber")

        val responseBuilder = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            responseBuilder.setSilenceCall(true)
        }

        respondToCall(callDetails, responseBuilder.build())
        phoneNumber?.let {
            incomingCallHelper.handleBlockedCall(context, phoneNumber)
        }
    }
}
