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

class PhoneStateReceiver : BroadcastReceiver() {
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
            logger.info("The phone is ringing! $incomingNumber")
        }
    }
}
