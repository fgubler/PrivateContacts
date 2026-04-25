/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.app.Activity

/** No-op implementation for fdroid: in-app review is not available */
suspend fun Activity.showAndroidReview(): Boolean = false
