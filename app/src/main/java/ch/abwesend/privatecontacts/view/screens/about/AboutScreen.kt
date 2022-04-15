package ch.abwesend.privatecontacts.view.screens.about

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.BulletPointListItem
import ch.abwesend.privatecontacts.view.components.LinkText
import ch.abwesend.privatecontacts.view.components.text.SectionTitle
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.routing.Screen.AboutTheApp
import ch.abwesend.privatecontacts.view.screens.BaseScreen
import ch.abwesend.privatecontacts.view.util.openLink
import ch.abwesend.privatecontacts.view.util.sendEmailMessage

object AboutScreen {
    @Composable
    fun Screen(screenContext: ScreenContext) {
        val context = LocalContext.current
        BaseScreen(screenContext = screenContext, selectedScreen = AboutTheApp) {
            Column(modifier = Modifier.padding(10.dp)) {
                AboutTheApp(context)
                ContactDevelopers(context)
                LegalDisclaimer()
            }
        }
    }

    @Composable
    private fun AboutTheApp(context: Context) {
        SectionTitle(titleRes = R.string.about_app_title, addTopPadding = false)
        Text(text = stringResource(id = R.string.about_app_description))
        Spacer(modifier = Modifier.height(5.dp))
        LazyColumn {
            item {
                BulletPointListItem {
                    LinkText(text = stringResource(id = R.string.play_store_entry)) {
                        openLink(
                            context,
                            "https://play.google.com/store/apps/details?id=ch.abwesend.privatecontacts"
                        )
                    }
                }
            }
            item {
                BulletPointListItem {
                    LinkText(text = stringResource(id = R.string.github_page)) {
                        openLink(context, "https://github.com/fgubler/PrivateContacts")
                    }
                }
            }
            item {
                BulletPointListItem {
                    LinkText(text = stringResource(id = R.string.privacy_title)) {
                        openLink(context, "https://www.privacypolicies.com/live/f40e5368-c69c-4530-9abc-60fef967ef93")
                    }
                }
            }
        }
    }

    @Composable
    private fun ContactDevelopers(context: Context) {
        SectionTitle(titleRes = R.string.contact_developers_title)
        Text(text = stringResource(id = R.string.contact_developers_text))
        Spacer(modifier = Modifier.height(5.dp))
        LazyColumn {
            item {
                BulletPointListItem {
                    LinkText(text = stringResource(id = R.string.report_error)) {
                        openLink(context, "https://github.com/fgubler/PrivateContacts/issues/new")
                    }
                }
            }
            item {
                BulletPointListItem {
                    LinkText(text = stringResource(id = R.string.write_email)) {
                        sendEmailMessage(context, "2Gusoft@gmail.com")
                    }
                }
            }
        }
    }

    @Composable
    private fun LegalDisclaimer() {
        SectionTitle(titleRes = R.string.legal_disclaimer_title)
        Text(text = stringResource(id = R.string.legal_disclaimer_text), fontStyle = FontStyle.Italic)
    }
}
