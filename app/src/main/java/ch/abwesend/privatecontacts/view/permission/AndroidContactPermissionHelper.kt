/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS

class AndroidContactPermissionHelper : PermissionHelperBase() {
    override val permissions: List<String> = listOf(READ_CONTACTS, WRITE_CONTACTS)
}
