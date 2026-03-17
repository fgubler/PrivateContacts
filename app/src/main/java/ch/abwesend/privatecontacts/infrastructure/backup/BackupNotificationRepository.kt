package ch.abwesend.privatecontacts.infrastructure.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import ch.abwesend.privatecontacts.R

class BackupNotificationRepository(private val context: Context) {
    private companion object {
        const val CHANNEL_ID = "ch.abwesend.privatecontacts.BackupChannel"
        const val NOTIFICATION_ID = 42_000
    }

    fun createForegroundInfo(): ForegroundInfo {
        ensureNotificationChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.backup_notification_title))
            .setContentText(context.getString(R.string.backup_notification_text))
            .setSmallIcon(R.drawable.ic_backup)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.backup_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.backup_notification_channel_description)
                setSound(null, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
