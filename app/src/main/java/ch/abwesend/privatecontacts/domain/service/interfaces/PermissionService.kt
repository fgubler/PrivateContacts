/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

interface PermissionService {
    fun hasContactReadPermission(): Boolean
    fun hasContactWritePermission(): Boolean
    fun hasReadAccountsPermission(): Boolean
    fun hasNotificationsPermission(): Boolean
}
