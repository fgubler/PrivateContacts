/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.service

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.getAnywhere

class AndroidPermissionService : PermissionService {
    override fun hasContactReadPermission(): Boolean =
        hasPermission(READ_CONTACTS)

    override fun hasContactWritePermission(): Boolean =
        hasPermission(WRITE_CONTACTS)

    override fun hasReadAccountsPermission(): Boolean =
        hasPermission(GET_ACCOUNTS)

    private fun hasPermission(permission: String): Boolean {
        val context: Context = getAnywhere()
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
