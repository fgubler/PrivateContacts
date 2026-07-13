/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SyncConflictResolutionTest {
    @Test
    fun `higher revision wins the total order`() {
        assertThat(
            SyncConflictResolution.firstCommandWins("bbb", "cmd1", "aaa", "cmd2")
        ).isTrue()
        assertThat(
            SyncConflictResolution.firstCommandWins("aaa", "cmd1", "bbb", "cmd2")
        ).isFalse()
    }

    @Test
    fun `equal revisions are broken by the command id`() {
        assertThat(
            SyncConflictResolution.firstCommandWins("aaa", "zzz", "aaa", "aaa")
        ).isTrue()
        assertThat(
            SyncConflictResolution.firstCommandWins("aaa", "aaa", "aaa", "zzz")
        ).isFalse()
    }

    @Test
    fun `two devices observing the same conflict pick the identical winner and fork`() {
        val originalGuid = "guid-original"
        val revisionA = "11111111-1111-1111-1111-111111111111"
        val revisionB = "22222222-2222-2222-2222-222222222222"

        // device X: local = A, incoming = B
        val deviceXIncomingWins = SyncConflictResolution.firstCommandWins(revisionB, "cmdB", revisionA, "cmdA")
        // device Y: local = B, incoming = A
        val deviceYIncomingWins = SyncConflictResolution.firstCommandWins(revisionA, "cmdA", revisionB, "cmdB")

        // both agree B wins (higher revision)
        assertThat(deviceXIncomingWins).isTrue()
        assertThat(deviceYIncomingWins).isFalse()

        // the loser (A) is forked identically on both devices
        val forkOnX = SyncConflictResolution.forkSyncId(originalGuid, revisionA)
        val forkOnY = SyncConflictResolution.forkSyncId(originalGuid, revisionA)
        assertThat(forkOnX).isEqualTo(forkOnY)

        val forkRevisionOnX = SyncConflictResolution.forkInitialRevision(forkOnX, revisionA)
        val forkRevisionOnY = SyncConflictResolution.forkInitialRevision(forkOnY, revisionA)
        assertThat(forkRevisionOnX).isEqualTo(forkRevisionOnY)
    }

    @Test
    fun `fork id differs from the original guid`() {
        val original = "guid-original"
        val losingRevision = "33333333-3333-3333-3333-333333333333"
        assertThat(SyncConflictResolution.forkSyncId(original, losingRevision)).isNotEqualTo(original)
    }
}
