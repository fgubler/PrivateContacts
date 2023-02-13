/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_PHONE_STATE

class CallPermissionHelper : PermissionHelperBase() {
    override val permissions: List<String> = listOf(READ_PHONE_STATE)
}
