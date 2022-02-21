/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

/**
 * Wrapper for a [IContactEditable] to allow breaking reference-equality for UI-updates
 * without having to copy too much.
 */
class ContactEditableWrapper(val contact: IContactEditable) : IContactEditable by contact {
    override fun wrap(): ContactEditableWrapper = ContactEditableWrapper(contact)
}
