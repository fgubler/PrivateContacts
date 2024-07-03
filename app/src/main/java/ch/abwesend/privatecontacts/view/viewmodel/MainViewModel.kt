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
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.initialization.InitializationState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InitialInfoDialog
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.NOT_AUTHENTICATED
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.SUCCESS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val dispatchers: IDispatchers by injectAnywhere()

    private val _initializationState: MutableState<InitializationState> = mutableStateOf(InitialInfoDialog)
    val initializationState: State<InitializationState> = _initializationState

    private val _authenticationStatus: MutableState<AuthenticationStatus> = mutableStateOf(NOT_AUTHENTICATED)
    val authenticationStatus: State<AuthenticationStatus> = _authenticationStatus

    fun goToNextState() {
        val oldState = _initializationState.value
        _initializationState.value = _initializationState.value.next()
        logger.debug(
            "Changed initializationState from ${oldState::class.java.simpleName} " +
                "to ${_initializationState.value::class.java.simpleName}"
        )
    }

    fun handleAuthenticationResult(authenticationFlow: Flow<AuthenticationStatus>) {
        viewModelScope.launch(dispatchers.default) {
            authenticationFlow.firstOrNull()?.let {
                _authenticationStatus.value = it
            }
        }
    }

    fun grantAccessWithoutAuthentication() {
        _authenticationStatus.value = SUCCESS
    }
}
