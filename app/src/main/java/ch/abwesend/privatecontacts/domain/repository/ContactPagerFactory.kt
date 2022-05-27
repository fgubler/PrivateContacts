/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import androidx.paging.Pager
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig

interface ContactPagerFactory {
    fun createSecretContactPager(searchConfig: ContactSearchConfig): Pager<Int, IContactBase>
    fun createAllContactPager(searchConfig: ContactSearchConfig): Pager<Int, IContactBase>
}
