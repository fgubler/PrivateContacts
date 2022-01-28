package ch.abwesend.privatecontacts.domain.repository

import androidx.paging.Pager
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase

interface ContactPagerFactory {
    fun createContactPager(): Pager<Int, IContactBase>
}
