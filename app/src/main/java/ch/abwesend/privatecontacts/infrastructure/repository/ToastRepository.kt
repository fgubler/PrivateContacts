/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.Context
import android.os.Looper
import android.widget.Toast
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.applicationScope
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.launch

class ToastRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    /**
     * Allows showing a toast from a background service.
     */
    fun showToastNotification(
        context: Context,
        message: String,
        length: Int = Toast.LENGTH_LONG,
    ) {
        applicationScope.launch(dispatchers.mainImmediate) {
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
