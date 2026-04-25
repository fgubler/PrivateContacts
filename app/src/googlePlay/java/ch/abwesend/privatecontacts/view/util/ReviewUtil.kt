/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.Settings
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import java.time.LocalDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Utility for Android user feedback / review */

/**
 * Tries to open the review UI of Android.
 * @return true if the action was successful
 */
suspend fun Activity.showAndroidReview(): Boolean {
    Settings.repository.latestUserPromptAtStartup = LocalDate.now()
    val reviewManager = ReviewManagerFactory.create(this)

    return prepareReview(this, reviewManager)?.let { reviewInfo ->
        showReview(this, reviewManager, reviewInfo)
    } ?: false
}

private suspend fun prepareReview(
    context: Context,
    reviewManager: ReviewManager
): ReviewInfo? = suspendCoroutine { continuation ->
    reviewManager.requestReviewFlow().addOnCompleteListener { preparationTask ->
        if (preparationTask.isSuccessful && preparationTask.result != null) {
            continuation.resume(preparationTask.result)
        } else {
            val errorCode = (preparationTask.exception as? ReviewException)?.errorCode?.toString() ?: "[NO_ERROR_CODE]"
            reviewManager.logger.info("Failed request review flow: errorCode = '$errorCode'")
            Toast.makeText(context, R.string.unexpected_error_apology, Toast.LENGTH_SHORT).show()
            continuation.resume(null)
        }
    }
}

/** @return true if successful */
private suspend fun showReview(
    activity: Activity,
    reviewManager: ReviewManager,
    reviewInfo: ReviewInfo,
): Boolean = suspendCoroutine { continuation ->
    reviewManager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener { task ->
        val success = task.isSuccessful
        if (success) {
            Settings.repository.showReviewDialog = false // already shown
            reviewManager.logger.info("Successfully tried showing review")
            Toast.makeText(activity, R.string.thank_you, Toast.LENGTH_SHORT).show()
        }
        continuation.resume(success)
    }
}
