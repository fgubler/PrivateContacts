package ch.abwesend.privatecontacts.domain.service

class EasterEggService {

    fun checkSearchForEasterEggs(query: String) {
        when (query) {
            KEYWORD_CRASH -> throw RuntimeException("App crashed on purpose!")
        }
    }

    companion object {
        val KEYWORD_CRASH = "CrashThisAppNow"
    }
}
