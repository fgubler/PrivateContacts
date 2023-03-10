/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_PHONE_STATE
import android.os.Build

class CallPermissionHelper : PermissionHelperBase() {
    override val permissions: List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(READ_PHONE_STATE, POST_NOTIFICATIONS)
        else listOf(READ_PHONE_STATE)
}
