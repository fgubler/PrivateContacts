/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult

/**
 * Show a dialog to monitor the progress of a long-running action and afterwards its result.
 * [onCloseDialog] should set the flow to [InactiveResource]
 */
@Composable
fun <T> ResourceFlowProgressAndResultDialog(
    flow: ResourceFlow<T>,
    onCloseDialog: () -> Unit,
    ErrorDialog: @Composable () -> Unit = { GenericUnknownErrorDialog(onClose = onCloseDialog) },
    ProgressDialog: @Composable () -> Unit = {
        SimpleProgressDialog(title = R.string.please_wait, allowRunningInBackground = false)
    },
    ResultDialog: @Composable (result: T, onCLose: () -> Unit) -> Unit,
) {
    val resource = flow.collectAsStateWithLifecycle(initialValue = InactiveResource()).value
    flow.logger.debug("Received resource of type ${resource.javaClass.simpleName}")

    when (resource) {
        is ErrorResource -> ErrorDialog()
        is InactiveResource -> { /* nothing to do */ }
        is LoadingResource -> ProgressDialog()
        is ReadyResource -> ResultDialog(resource.value, onCloseDialog)
    }
}

/**
 * Show a dialog to monitor the progress of a long-running action and afterwards its result.
 * [onCloseDialog] should set the flow to [InactiveResource].
 * This is a kind of bridge between the Result-world and the Resource-world.
 */
@Composable
fun <TSuccess, TError> ResultBasedResourceFlowProgressAndResultDialog(
    flow: ResourceFlow<BinaryResult<TSuccess, TError>>,
    onCloseDialog: () -> Unit,
    errorDialog: @Composable (TError?) -> Unit = { GenericUnknownErrorDialog(onClose = onCloseDialog) },
    progressDialog: @Composable () -> Unit = {
        SimpleProgressDialog(title = R.string.please_wait, allowRunningInBackground = false)
    },
    resultDialog: @Composable (result: TSuccess, onClose: () -> Unit) -> Unit,
) {
    val resource = flow.collectAsStateWithLifecycle(initialValue = InactiveResource()).value
    flow.logger.debug("Received resource of type ${resource.javaClass.simpleName}")

    when (resource) {
        is ErrorResource -> errorDialog(null)
        is InactiveResource -> { /* nothing to do */ }
        is LoadingResource -> progressDialog()
        is ReadyResource -> {
            when (val result = resource.value) {
                is ErrorResult -> errorDialog(result.error)
                is SuccessResult -> resultDialog(result.value, onCloseDialog)
            }
        }
    }
}
