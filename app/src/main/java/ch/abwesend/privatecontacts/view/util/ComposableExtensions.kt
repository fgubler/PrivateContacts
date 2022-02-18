package ch.abwesend.privatecontacts.view.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.InactiveResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType

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
