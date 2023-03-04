/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

interface IContactBaseWithAccountInformation : IContactBase {
    var saveInAccount: ContactAccount
}

data class ContactBaseWithAccountInformation(
    private val contactBase: IContactBase,
    override var saveInAccount: ContactAccount
) : IContactBase by contactBase, IContactBaseWithAccountInformation

fun IContactBase.withAccountInformation(): IContactBaseWithAccountInformation =
    if (this is IContactBaseWithAccountInformation) this
    else ContactBaseWithAccountInformation(contactBase = this, saveInAccount = ContactAccount.AppInternal)
