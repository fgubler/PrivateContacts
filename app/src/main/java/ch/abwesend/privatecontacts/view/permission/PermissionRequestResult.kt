/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

enum class PermissionRequestResult(val usable: Boolean) {
    ALREADY_GRANTED(usable = true),
    NEWLY_GRANTED(usable = true),
    PARTIALLY_NEWLY_GRANTED(usable = true),
    DENIED(usable = false),
    ERROR(usable = false),
}
