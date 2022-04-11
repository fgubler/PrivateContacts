/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import kotlinx.coroutines.delay

/**
 * need to wait for the keyboard to be open...
 */
@ExperimentalFoundationApi
suspend fun BringIntoViewRequester.bringIntoViewDelayed(delayMs: Long = 500) {
    delay(delayMs)
    bringIntoView()
}
