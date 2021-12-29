package ch.abwesend.privatecontacts.domain.model.contact

import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber

interface Contact : ContactBase {
    val phoneNumbers: List<PhoneNumber>
}

data class ContactFull(
    private val contactBase: ContactBase,
    override var phoneNumbers: List<PhoneNumber>,
) : ContactBase by contactBase, Contact
