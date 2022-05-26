/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.calldetection

import android.content.Context
import android.os.Looper
import android.widget.Toast
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.applicationScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToastRepository {
    /**
     * Allows showing a toast from a background service.
     */
    fun showToastNotification(
        context: Context,
        message: String,
        length: Int = Toast.LENGTH_LONG,
    ) {
        applicationScope.launch(Dispatchers.Main.immediate) {
            try {
                Toast.makeText(context.applicationContext, message, length).show()
            } catch (t: Throwable) {
                logger.warning("Failed to show toast message for incoming call. Try with java-style.", t)
                showToastNotificationJavaStyle(context, message, length)
            }
        }
    }

    /**
     * Same as [showToastNotification] but without kotlin and coroutines
     */
    private fun showToastNotificationJavaStyle(
        context: Context,
        message: String,
        length: Int = Toast.LENGTH_LONG,
    ) {
        Thread {
            try {
                Looper.prepare()
                Toast.makeText(context.applicationContext, message, length).show()
                Looper.loop()
            } catch (t: Throwable) {
                logger.warning("Failed to show toast message for incoming call", t)
            }
        }.start()
    }
}
