/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.initialization.InitializationState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InitialInfoDialog

class MainViewModel : ViewModel() {

    private val _initializationState: MutableState<InitializationState> = mutableStateOf(InitialInfoDialog)
    val initializationState: State<InitializationState> = _initializationState

    fun goToNextState() {
        val oldState = _initializationState.value
        _initializationState.value = _initializationState.value.next()
        logger.debug(
            "Changed initializationState from ${oldState::class.java.simpleName} " +
                "to ${_initializationState.value::class.java.simpleName}"
        )
    }
}
