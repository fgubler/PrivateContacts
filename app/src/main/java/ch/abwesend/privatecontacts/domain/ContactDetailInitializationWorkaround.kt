/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain

/** This is an experiment: let us see whether it will actually work. */
object ContactDetailInitializationWorkaround {
    /**
     * A workaround for the problem that the app is reopened on the contact-detail screen after a long pause
     * and it tries to re-load the contact but fails to do so.
     * => check if the app has ever (internally) navigated to the contact-detail screen.
     */
    var hasOpenedContact: Boolean = false
}
