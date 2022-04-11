/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import java.util.UUID

private const val CHANNEL_ID = "ch.abwesend.privatecontacts.IncomingCallNotificationChannel"

fun Context.showIncomingCallNotification(callerNumber: String, contactNames: List<String>) {
    if (contactNames.isEmpty()) {
        logger.debug("No matching contacts: do not show a notification.")
        return
    }

    createNotificationChannel()

    val title = getString(R.string.incoming_call_title)
    val text = if (contactNames.size == 1) {
        getString(R.string.incoming_call_text_single_match, contactNames.first())
    } else {
        val namesConcat = contactNames.joinToString(separator = ", ") { "\"$it\"" }
        getString(
            R.string.incoming_call_text_multiple_matches,
            contactNames.first(),
            callerNumber,
            namesConcat,
        )
    }

    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baseline_phone_callback_24)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    val manager = NotificationManagerCompat.from(this)
    val notificationId = UUID.randomUUID().hashCode()
    manager.notify(notificationId, builder.build())
}

private fun Context.createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = getString(R.string.incoming_call_channel_title)
        val descriptionText = getString(R.string.incoming_call_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
