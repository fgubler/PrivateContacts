/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

interface IPermissionProvider {
    val callPermissionHelper: CallPermissionHelper
    val contactPermissionHelper: AndroidContactPermissionHelper
    val callScreeningRoleHelper: CallScreeningRoleHelper
}

data class PermissionProvider(
    override val callPermissionHelper: CallPermissionHelper,
    override val contactPermissionHelper: AndroidContactPermissionHelper,
    override val callScreeningRoleHelper: CallScreeningRoleHelper,
) : IPermissionProvider
