/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.Settings

class AndroidContactPermissionHelper : PermissionHelperBase() {
    override val permissions: List<String> = listOf(READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS)

    fun requestAndroidContactPermissions(onResult: (PermissionRequestResult) -> Unit) {
        logger.debug("Checking permissions for accessing Android contacts")

        val onPermissionResult: (PermissionRequestResult) -> Unit = { result ->
            when (result) {
                PermissionRequestResult.NEWLY_GRANTED, PermissionRequestResult.PARTIALLY_NEWLY_GRANTED -> {
                    Settings.repository.showAndroidContacts = true
                    val postfix = if (result == PermissionRequestResult.NEWLY_GRANTED) "" else " partially"
                    logger.debug("Android contacts: permissions granted$postfix")
                }
                PermissionRequestResult.DENIED -> {
                    logger.debug("Android contacts: permissions denied")
                }
                PermissionRequestResult.ERROR -> {
                    logger.debug("Android contacts: error while requesting permissions")
                }
                PermissionRequestResult.ALREADY_GRANTED -> {
                    logger.debug("Android contacts: permissions already granted")
                }
            }
            onResult(result)
        }

        requestUserPermissionsNow(onPermissionResult)
    }
}
