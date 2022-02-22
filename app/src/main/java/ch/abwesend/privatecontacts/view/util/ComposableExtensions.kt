/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.content.Context
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfig
import ch.abwesend.privatecontacts.view.model.config.IconButtonConfigGeneric

@Composable
fun getLogger(): ILogger = LocalContext.current.logger

@Composable
fun <T> AsyncResource<T>.composeIfReady(handler: @Composable (T) -> Unit): AsyncResource<T> {
    (this as? ReadyResource)?.let { handler(value) }
    return this
}

@Composable
fun <T> AsyncResource<T>.composeIfLoading(handler: @Composable () -> Unit): AsyncResource<T> {
    (this as? LoadingResource)?.let { handler() }
    return this
}

@Composable
fun <T> AsyncResource<T>.composeIfError(handler: @Composable (List<Exception>) -> Unit): AsyncResource<T> {
    (this as? ErrorResource)?.let { handler(it.errors) }
    return this
}

@Composable
fun <T> AsyncResource<T>.composeIfInactive(handler: @Composable () -> Unit): AsyncResource<T> {
    (this as? InactiveResource)?.let { handler() }
    return this
}

@Composable
fun ContactDataType.getTitle(context: Context = LocalContext.current): String {
    return getTitle(context::getString)
}

@ExperimentalComposeUiApi
@Composable
fun createKeyboardAndFocusManager(): KeyboardAndFocusManager {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    return KeyboardAndFocusManager(keyboardController, focusManager)
}

@Composable
fun <T> IconButtonConfigGeneric<T>.toIconButton(listenerInput: T) {
    IconButton(onClick = { onClick(listenerInput) }) {
        Icon(imageVector = icon, contentDescription = stringResource(id = label))
    }
}

@Composable
fun <T> IconButtonConfig.toIconButton() = toIconButton(listenerInput = Unit)
