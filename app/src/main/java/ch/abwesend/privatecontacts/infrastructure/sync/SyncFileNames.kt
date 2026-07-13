/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync

/**
 * Encodes/decodes the flat Drive app-data namespace. All routing lives in the filename, so there
 * are no folders to create and nothing to race on.
 *
 * A deviceId is a UUID (hyphen-separated, never underscores), so splitting on '_' is unambiguous.
 */
object SyncFileNames {
    const val PROTOCOL_FILE = "protocol.json"
    const val KEYCHECK_FILE = "keycheck.json"
    const val CMD_PREFIX = "cmd_"
    const val BATCH_PREFIX = "batch_"
    const val SNAPSHOT_PREFIX = "snap_"
    const val SNAPSHOT_INDEX_PREFIX = "snapidx_"
    const val SNAPSHOT_INDEX_SUFFIX = ".json"

    private const val SEQ_WIDTH = 6

    fun formatSequence(sequence: Long): String = sequence.toString().padStart(SEQ_WIDTH, '0')

    fun commandFileName(deviceId: String, sequence: Long): String =
        "$CMD_PREFIX${deviceId}_${formatSequence(sequence)}"

    fun batchFileName(deviceId: String, fromSequence: Long, toSequence: Long): String =
        "$BATCH_PREFIX${deviceId}_${formatSequence(fromSequence)}-${formatSequence(toSequence)}"

    fun snapshotFileName(snapshotId: String): String = "$SNAPSHOT_PREFIX$snapshotId"

    fun snapshotIndexFileName(snapshotId: String): String =
        "$SNAPSHOT_INDEX_PREFIX$snapshotId$SNAPSHOT_INDEX_SUFFIX"

    /** a parsed log-file name (a single-command `cmd_` has fromSequence == toSequence) */
    data class ParsedLogFile(val deviceId: String, val fromSequence: Long, val toSequence: Long)

    fun parseLogFile(fileName: String): ParsedLogFile? =
        when {
            fileName.startsWith(CMD_PREFIX) -> parseCommand(fileName)
            fileName.startsWith(BATCH_PREFIX) -> parseBatch(fileName)
            else -> null
        }

    private fun parseCommand(fileName: String): ParsedLogFile? {
        val body = fileName.removePrefix(CMD_PREFIX)
        val separatorIndex = body.lastIndexOf('_')
        val parsed = if (separatorIndex <= 0) {
            null
        } else {
            val deviceId = body.substring(0, separatorIndex)
            val sequence = body.substring(separatorIndex + 1).toLongOrNull()
            sequence?.let { ParsedLogFile(deviceId, it, it) }
        }
        return parsed
    }

    private fun parseBatch(fileName: String): ParsedLogFile? {
        val body = fileName.removePrefix(BATCH_PREFIX)
        val separatorIndex = body.lastIndexOf('_')
        val parsed = if (separatorIndex <= 0) {
            null
        } else {
            val deviceId = body.substring(0, separatorIndex)
            val range = body.substring(separatorIndex + 1).split('-')
            val fromSequence = range.getOrNull(0)?.toLongOrNull()
            val toSequence = range.getOrNull(1)?.toLongOrNull()
            if (fromSequence != null && toSequence != null) {
                ParsedLogFile(deviceId, fromSequence, toSequence)
            } else {
                null
            }
        }
        return parsed
    }

    fun parseSnapshotId(fileName: String): String? =
        when {
            fileName.startsWith(SNAPSHOT_INDEX_PREFIX) ->
                fileName.removePrefix(SNAPSHOT_INDEX_PREFIX).removeSuffix(SNAPSHOT_INDEX_SUFFIX)
            fileName.startsWith(SNAPSHOT_PREFIX) -> fileName.removePrefix(SNAPSHOT_PREFIX)
            else -> null
        }
}
