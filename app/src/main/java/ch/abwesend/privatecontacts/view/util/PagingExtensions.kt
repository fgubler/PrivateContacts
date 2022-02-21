/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

val <T : Any> LazyPagingItems<T>.isLoading: Boolean
    get() = states.any { it is LoadState.Loading }

val <T : Any> LazyPagingItems<T>.isError: Boolean
    get() = states.any { it is LoadState.Error }

private val <T : Any> LazyPagingItems<T>.states: List<LoadState>
    get() = listOf(
        loadState.refresh,
        loadState.append,
        loadState.prepend
    )
