package ch.abwesend.privatecontacts.view.initialization

import ch.abwesend.privatecontacts.R

/**
 * Contains release notes for each version of the app.
 * The list should be sorted by version code in descending order (newest first).
 */
object ReleaseNotes {
    fun getReleaseNotesBetween(fromVersion: Int, toVersion: Int): List<ReleaseNote> {
        return releaseNotes
            .filter { it.versionCode in fromVersion..toVersion }
            .sortedByDescending { it.versionCode }
    }

    private val releaseNotes = listOf(
        ReleaseNote(
            versionCode = 84,
            textResourceIds = listOf(R.string.release_notes_v84_1, R.string.release_notes_v84_2)
        ),
        ReleaseNote(
            versionCode = 85,
            textResourceIds = listOf(R.string.release_notes_v85)
        ),
        // Add more release notes as needed
    )
}

data class ReleaseNote(
    val versionCode: Int,
    val textResourceIds: List<Int>
)
