/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.util.deterministicUuid

/**
 * Pure, deterministic conflict-resolution primitives. Every device computes byte-identical results,
 * so N devices observing one conflict converge on exactly two contacts (winner + one fork copy).
 */
object SyncConflictResolution {
    /**
     * Total order over two competing commands: the greater [newRevision] wins, ties broken by the
     * greater [commandId]. Both are compared as (already lower-cased) UUID strings.
     *
     * @return true if the first command wins over the second.
     */
    fun firstCommandWins(
        firstNewRevision: String,
        firstCommandId: String,
        secondNewRevision: String,
        secondCommandId: String,
    ): Boolean {
        val revisionComparison = firstNewRevision.compareTo(secondNewRevision)
        return if (revisionComparison != 0) {
            revisionComparison > 0
        } else {
            firstCommandId.compareTo(secondCommandId) >= 0
        }
    }

    /**
     * Deterministic id of the fork copy `g'`. Seeded with the losing revision (a UUID) rather than
     * the losing command-id: both devices agree on which revision lost (the lower in the total order)
     * and its value, so the fork id is byte-identical everywhere without needing to persist the local
     * command-id.
     */
    fun forkSyncId(originalSyncId: String, losingRevision: String): String =
        deterministicUuid(originalSyncId, losingRevision).toString()

    /** Deterministic initial revision of the fork copy, so a later edit fast-forwards on every peer. */
    fun forkInitialRevision(forkSyncId: String, losingRevision: String): String =
        deterministicUuid(forkSyncId, losingRevision).toString()
}
