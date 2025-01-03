package ch.abwesend.privatecontacts.view.screens.about

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.UserFeedbackPseudoException
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.components.BulletPointListItem
import ch.abwesend.privatecontacts.view.components.LinkText
import ch.abwesend.privatecontacts.view.components.text.SectionSubtitle
import ch.abwesend.privatecontacts.view.components.text.SectionTitle
import ch.abwesend.privatecontacts.view.model.screencontext.IScreenContextBase
import ch.abwesend.privatecontacts.view.routing.Screen.AboutTheApp
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.util.bringIntoViewDelayed
import ch.abwesend.privatecontacts.view.util.getCurrentActivity
import ch.abwesend.privatecontacts.view.util.openLink
import ch.abwesend.privatecontacts.view.util.sendEmailMessage
import ch.abwesend.privatecontacts.view.util.showAndroidReview
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts

@ExperimentalFoundationApi
@ExperimentalContracts
object AboutScreen {
    @Composable
    fun Screen(screenContext: IScreenContextBase) {
        val context = LocalContext.current
        BaseScreen(screenContext = screenContext, selectedScreen = AboutTheApp) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                AboutTheApp(context)
                ContactDevelopers(context)
                LegalDisclaimer()
            }
        }
    }

    @Composable
    private fun AboutTheApp(context: Context) {
        val activity = getCurrentActivity()
        val coroutineScope = rememberCoroutineScope()

        SectionTitle(titleRes = R.string.about_app_title, addTopPadding = false)
        Text(text = stringResource(id = R.string.about_app_description))
        Spacer(modifier = Modifier.height(5.dp))
        Column {
            BulletPointListItem {
                LinkText(text = stringResource(id = R.string.play_store_entry)) {
                    openLink(
                        context,
                        "https://play.google.com/store/apps/details?id=ch.abwesend.privatecontacts"
                    )
                }
            }
            activity?.let { activity ->
                BulletPointListItem {
                    LinkText(text = stringResource(id = R.string.in_app_review)) {
                        coroutineScope.launch {
                            activity.showAndroidReview()
                        }
                    }
                }
            }
            BulletPointListItem {
                LinkText(text = stringResource(id = R.string.github_page)) {
                    openLink(context, "https://github.com/fgubler/PrivateContacts")
                }
            }
            BulletPointListItem {
                LinkText(text = stringResource(id = R.string.privacy_title)) {
                    openLink(context, "https://www.privacypolicies.com/live/f40e5368-c69c-4530-9abc-60fef967ef93")
                }
            }
        }
    }

    @Composable
    private fun ContactDevelopers(context: Context) {
        SectionTitle(titleRes = R.string.contact_developers_title)
        Text(text = stringResource(id = R.string.contact_developers_text))
        Spacer(modifier = Modifier.height(5.dp))
        Column {
            BulletPointListItem {
                LinkText(text = stringResource(id = R.string.report_error_github)) {
                    openLink(context, "https://github.com/fgubler/PrivateContacts/issues/new")
                }
            }
            BulletPointListItem {
                LinkText(text = stringResource(id = R.string.write_email)) {
                    sendEmailMessage(context, "2Gusoft@gmail.com")
                }
            }

            ReportBugField()
        }
    }

    @Composable
    private fun ReportBugField() {
        var errorText: String by remember { mutableStateOf("") }
        val context = LocalContext.current

        val viewRequester = remember { BringIntoViewRequester() }
        val coroutineScope = rememberCoroutineScope()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            OutlinedTextField(
                label = { Text(text = stringResource(id = R.string.report_error)) },
                value = errorText,
                onValueChange = { newValue -> errorText = newValue },
                singleLine = false,
                modifier = Modifier
                    .heightIn(min = 100.dp)
                    .weight(1F)
                    .onFocusChanged {
                        if (it.isFocused) {
                            coroutineScope.launch {
                                viewRequester.bringIntoViewDelayed()
                            }
                        }
                    }
            )
            Button(
                modifier = Modifier.padding(start = 5.dp),
                enabled = errorText.isNotEmpty(),
                onClick = {
                    sendUserReportAsException(errorText)
                    Toast.makeText(context, R.string.error_report_sent_confirmation, Toast.LENGTH_SHORT).show()
                    errorText = ""
                },
            ) {
                Text(text = stringResource(id = R.string.send))
            }
        }
    }

    private fun sendUserReportAsException(text: String) {
        if (text.isNotEmpty()) {
            val fullText = "Received user-feedback: $text"
            logger.warning(fullText)
            logger.logToCrashlytics(UserFeedbackPseudoException(fullText), overridePreferences = true)
        }
    }

    @Composable
    private fun LegalDisclaimer() {
        SectionTitle(titleRes = R.string.legal_disclaimer_title)
        Text(text = stringResource(id = R.string.legal_disclaimer_text), fontStyle = FontStyle.Italic)
        Spacer(modifier = Modifier.height(5.dp))
        SectionSubtitle(titleRes = R.string.legal_disclaimer_whatsapp_title)
        Text(text = stringResource(id = R.string.legal_disclaimer_whatsapp_text), fontStyle = FontStyle.Italic)
        SectionSubtitle(titleRes = R.string.privacy_advertisement_id_title)
        Text(text = stringResource(id = R.string.advertisement_id_information_text))
    }
}
