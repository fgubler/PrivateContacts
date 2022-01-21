package ch.abwesend.privatecontacts.view.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.LoadingResource
import ch.abwesend.privatecontacts.domain.lib.flow.ReadyResource
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType

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
fun ContactDataSubType.getTitle(context: Context = LocalContext.current): String {
    return getTitle(context::getString)
}
