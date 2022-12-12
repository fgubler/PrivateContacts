/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.testutil

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

fun mockUriParse() {
    mockkStatic(Uri::class)
    every { Uri.parse(any()) } answers {
        val uriString = firstArg<String>()
        val mock = mockk<Uri>(relaxed = true)
        every { mock.toString() } returns uriString
        mock
    }
}
