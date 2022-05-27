/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import android.os.Build

inline fun ifAndroidVersion(versionCode: Int, operation: () -> Unit) {
    if (Build.VERSION.SDK_INT >= versionCode) {
        operation()
    }
}
