/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.canReadCallingNumberFromPhoneState
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

/**
 * Handle an incoming call via broadcast-receiver.
 * Use this as fallback if using [CallScreeningService] is not an option
 */
class PhoneStateReceiver : BroadcastReceiver() {
    private val notificationRepository: CallNotificationRepository by injectAnywhere()
    private val settings: SettingsRepository by injectAnywhere()
    private val incomingCallHelper: IncomingCallHelper by injectAnywhere()

    init {
        settings // just to make sure it is being loaded
    }

    override fun onReceive(context: Context, intent: Intent?) {
        logger.debug("Receiving broadcast")

        if (!settings.observeIncomingCalls) {
            logger.debug("Broadcast-Receiver is turned off in settings")
            return
        }

        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            // either no intent or an invalid action
            logger.debug("Ignoring invalid broadcast $intent")
            return
        }

        val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        logger.debug("Received phone state $phoneState")

        when (phoneState) {
            TelephonyManager.EXTRA_STATE_RINGING -> onRinging(context, intent)
            TelephonyManager.EXTRA_STATE_IDLE -> onIdle(context)
            else -> { }
        }
    }

    @Suppress("DEPRECATION")
    private fun onRinging(context: Context, intent: Intent) {
        if (canReadCallingNumberFromPhoneState) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            logger.debug("Receiving a call from $incomingNumber")
            incomingNumber?.let {
                applicationScope.launch {
                    incomingCallHelper.handleIncomingCall(context, it)
                }
            }
        }
    }

    private fun onIdle(context: Context) {
        logger.debug("Phone state idle: close notifications")
        notificationRepository.cancelNotifications(context)
    }
}
