/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/**
 * Helper-method to collect [Flow]s carrying events (i.e. we don't want to have them as state)
 */
@SuppressLint("ComposableNaming")
@Composable
inline fun <T> Flow<T>.collectWithEffect(crossinline observer: (T) -> Unit) {
    LaunchedEffect(Unit) {
        collect { observer(it) }
    }
}
