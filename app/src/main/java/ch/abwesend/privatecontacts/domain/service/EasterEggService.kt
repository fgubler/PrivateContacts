package ch.abwesend.privatecontacts.domain.service

import androidx.annotation.VisibleForTesting

class EasterEggService {

    fun checkSearchForEasterEggs(query: String) {
        when (query) {
            KEYWORD_CRASH -> throw RuntimeException("App crashed on purpose!")
        }
    }

    companion object {
        @VisibleForTesting
        internal const val KEYWORD_CRASH = "CrashThisAppNow"
    }
}
