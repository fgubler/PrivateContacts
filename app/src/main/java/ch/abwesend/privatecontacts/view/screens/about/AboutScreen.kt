package ch.abwesend.privatecontacts.view.screens.about

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
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
import ch.abwesend.privatecontacts.view.util.openLink
import ch.abwesend.privatecontacts.view.util.sendEmailMessage

object AboutScreen {
    @Composable
    fun Screen() {
        val context = LocalContext.current
        val bulletPointModifier = Modifier.padding(start = 10.dp)

        Column(modifier = Modifier.padding(10.dp)) {
            AboutTheApp(context, bulletPointModifier)
            Spacer(modifier = Modifier.height(20.dp))
            ContactDevelopers(context, bulletPointModifier)
            Spacer(modifier = Modifier.height(20.dp))
            LegalDisclaimer()
        }
    }

    @Composable
    private fun SectionTitle(@StringRes titleRes: Int) {
        Text(
            text = stringResource(id = titleRes),
            style = MaterialTheme.typography.h5,
        )
    }

    @Composable
    private fun AboutTheApp(context: Context, bulletPointModifier: Modifier) {
        SectionTitle(titleRes = R.string.about_app_title)
        Text(text = stringResource(id = R.string.about_app_description))
        LazyColumn(modifier = bulletPointModifier) {
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
                        openLink(context, "https://github.com/fgubler/PrivateContacts")
                    }
                }
            }
        }
    }

    @Composable
    private fun ContactDevelopers(context: Context, bulletPointModifier: Modifier) {
        SectionTitle(titleRes = R.string.contact_developers_title)

        LazyColumn(modifier = bulletPointModifier) {
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
