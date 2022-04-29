/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.model

enum class DatabaseResetState {
    INITIAL,
    RUNNING,
    RUNNING_IN_BACKGROUND,
    SUCCESSFUL,
    FAILED,
}
