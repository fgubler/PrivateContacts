/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.repository.ContactPagerFactory
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import java.lang.Exception

class ContactPagingSource(
    private val searchConfig: ContactSearchConfig
) : PagingSource<Int, IContactBase>() {

    private val contactRepository: IContactRepository by injectAnywhere()

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, IContactBase> {
        val pageNumber = params.key ?: 1 // start on page 1 by default
        val loadSize = params.loadSize

        return try {
            val contacts = contactRepository.getContactsPaged(
                searchConfig = searchConfig,
                loadSize = loadSize,
                offsetInRows = (pageNumber - 1) * PAGE_SIZE,
            )

            LoadResult.Page(
                data = contacts,
                prevKey = if (pageNumber <= 1) null else pageNumber - 1,
                nextKey = if (contacts.isEmpty()) null else pageNumber + loadSize / PAGE_SIZE
            )
        } catch (e: Exception) {
            logger.error("Failed to load contacts with loadSize = $loadSize and page = $pageNumber", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, IContactBase>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object : ContactPagerFactory {
        private const val PAGE_SIZE = 50

        override fun createSecretContactPager(searchConfig: ContactSearchConfig): Pager<Int, IContactBase> =
            Pager(
                PagingConfig(
                    pageSize = PAGE_SIZE,
                    maxSize = 10 * PAGE_SIZE,
                ),
                pagingSourceFactory = { ContactPagingSource(searchConfig) }
            )

        override fun createAllContactPager(searchConfig: ContactSearchConfig): Pager<Int, IContactBase> {
            // TODO implement
            return createSecretContactPager(searchConfig)
        }
    }
}
