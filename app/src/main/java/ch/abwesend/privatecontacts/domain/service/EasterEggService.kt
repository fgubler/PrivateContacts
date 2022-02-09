package ch.abwesend.privatecontacts.domain.service

class EasterEggService {

    fun checkSearchForEasterEggs(query: String) {
        when (query) {
            KEYWORD_CRASH -> throw RuntimeException("App crashed on purpose!")
        }
    }

    companion object {
        private const val KEYWORD_CRASH = "CrashThisAppNow"
    }
}
