/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.core.app.NotificationManagerCompat
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.Settings
import java.util.UUID

private const val CHANNEL_ID = "ch.abwesend.privatecontacts.IncomingCallNotificationChannel"
private const val MAX_NOTIFICATION_TIMEOUT_MS = 120000L // 2min

class NotificationRepository {
    private val potentiallyActiveNotifications = mutableSetOf<Int>()

    fun cancelNotifications(context: Context) {
        // cancelAll may be expensive: do not call if clearly unnecessary
        if (potentiallyActiveNotifications.isNotEmpty()) {
            context.notificationManager.cancelAll()
            potentiallyActiveNotifications.clear()
        }
    }

    fun showIncomingCallNotification(
        context: Context,
        callerNumber: String,
        contactNames: List<String>
    ) {
        if (contactNames.isEmpty()) {
            logger.debug("No matching contacts: do not show a notification.")
            return
        }

        context.createNotificationChannel()

        val title = context.getString(R.string.incoming_call_title)
        val text = if (contactNames.size == 1) {
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

        val visibility =
            if (Settings.current.showIncomingCallsOnLockScreen) VISIBILITY_PUBLIC
            else VISIBILITY_SECRET

        val intent = navigateToCallScreenIntent
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_phone_callback_24)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(visibility)
            .setTimeoutAfter(MAX_NOTIFICATION_TIMEOUT_MS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(context)
        val notificationId = UUID.randomUUID().hashCode()
        manager.notify(notificationId, builder.build())
        potentiallyActiveNotifications.add(notificationId)
    }

    private fun Context.createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.incoming_call_channel_title)
            val descriptionText = getString(R.string.incoming_call_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val Context.notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val navigateToCallScreenIntent
        get() = Intent(Intent.ACTION_CALL_BUTTON)
}
