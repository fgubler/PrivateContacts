/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

interface ISyncScheduler {
    /** debounced one-shot upload (coalesces a burst of local saves into one run) */
    fun scheduleUploadDebounced()

    /** periodic background pull */
    fun schedulePeriodicPull()

    /** one-shot pull for app-start and manual "Sync now" */
    fun triggerPullNow()
}
