/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

/**
 * Writes a tiny encrypted `keycheck.json` verifier on the first device and, on later devices,
 * decrypts it synchronously in the enable flow so a wrong passphrase is rejected immediately (issue 9).
 */
interface ISyncKeyCheckService {
    enum class Outcome {
        /** the keycheck decrypted correctly - the passphrase matches */
        MATCH,

        /** no keycheck existed yet - this device initialized it (first device) */
        INITIALIZED,

        /** the keycheck exists but did not decrypt - the passphrase is wrong */
        MISMATCH,

        /** a network/other error prevented verification */
        ERROR,
    }

    suspend fun verifyOrInitialize(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
    ): Outcome
}
