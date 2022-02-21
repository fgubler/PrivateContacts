/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

fun Intent.tryStartActivity(context: Context) {
    if (resolveActivity(context.packageManager) != null) {
        ContextCompat.startActivity(context, this, null)
    }
}