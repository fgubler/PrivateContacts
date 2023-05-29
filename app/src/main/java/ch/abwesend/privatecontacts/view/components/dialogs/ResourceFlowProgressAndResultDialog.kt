/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger

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
    ResultDialog: @Composable (T) -> Unit,
) {
    val resource = flow.collectAsState(initial = InactiveResource()).value
    flow.logger.debug("Received resource of type ${resource.javaClass.simpleName}")

    when (resource) {
        is ErrorResource -> ErrorDialog()
        is InactiveResource -> { /* nothing to do */ }
        is LoadingResource -> ProgressDialog()
        is ReadyResource -> ResultDialog(resource.value)
    }
}
