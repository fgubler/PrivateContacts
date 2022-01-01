package ch.abwesend.privatecontacts.domain.lib.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ApplicationScope(
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : CoroutineScope by coroutineScope
