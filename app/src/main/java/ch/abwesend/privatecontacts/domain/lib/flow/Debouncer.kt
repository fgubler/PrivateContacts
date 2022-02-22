/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.lib.flow

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@FlowPreview
class Debouncer<T>(private val scope: CoroutineScope, debounceMs: Long) {
    private val flow = MutableSharedFlow<T>()
    private val debouncedFlow = flow.debounce(debounceMs)

    fun newValue(value: T) {
        scope.launch { flow.emit(value) }
    }

    companion object {
        fun <T> debounce(
            scope: CoroutineScope,
            debounceMs: Long,
            action: (T) -> Unit
        ): Debouncer<T> {
            val debouncer = Debouncer<T>(scope, debounceMs)

            val dispatchers: IDispatchers = getAnywhere()
            scope.launch(dispatchers.default) {
                debouncer.debouncedFlow.collect { value ->
                    action(value)
                }
            }

            return debouncer
        }
    }
}
