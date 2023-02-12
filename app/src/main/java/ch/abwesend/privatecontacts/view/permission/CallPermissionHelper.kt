/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_PHONE_STATE

class CallPermissionHelper : PermissionHelperBase() {
    // TODO GET_ACCOUNTS does not really belong here: is only needed to create public contacts
    //  => move during refactoring of permission-logic
    override val permissions: List<String> = listOf(READ_PHONE_STATE, GET_ACCOUNTS)
}
