package ch.abwesend.privatecontacts.view.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ch.abwesend.privatecontacts.domain.lib.logging.ILogger
import ch.abwesend.privatecontacts.domain.lib.logging.logger

@Composable
fun getLogger(): ILogger = LocalContext.current.logger
