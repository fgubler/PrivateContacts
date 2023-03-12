/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory

import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.AndroidContactMutable
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.MutableContact
import com.alexstyl.contactstore.mutableCopy

interface IAndroidContactMutableFactory {
    fun toAndroidContactMutable(contact: Contact): IAndroidContactMutable
    fun create(): IAndroidContactMutable
}

class AndroidContactMutableFactory : IAndroidContactMutableFactory {
    override fun toAndroidContactMutable(contact: Contact): IAndroidContactMutable {
        val mutableContact = contact.mutableCopy()
        return AndroidContactMutable(mutableContact)
    }

    override fun create(): IAndroidContactMutable {
        val mutableContact = MutableContact()
        return AndroidContactMutable(mutableContact)
    }
}
