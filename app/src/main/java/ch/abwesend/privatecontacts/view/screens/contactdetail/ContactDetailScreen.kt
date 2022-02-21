/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactdetail

import androidx.annotation.StringRes
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.flow.AsyncResource
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.view.components.FullScreenError
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullScreen
import ch.abwesend.privatecontacts.view.components.buttons.BackIconButton
import ch.abwesend.privatecontacts.view.components.buttons.EditIconButton
import ch.abwesend.privatecontacts.view.components.buttons.MoreActionsIconButton
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.model.config.ButtonConfig
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.Screen
import ch.abwesend.privatecontacts.view.util.composeIfError
import ch.abwesend.privatecontacts.view.util.composeIfInactive
import ch.abwesend.privatecontacts.view.util.composeIfLoading
import ch.abwesend.privatecontacts.view.util.composeIfReady
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import kotlinx.coroutines.FlowPreview

@ExperimentalComposeUiApi
@FlowPreview
object ContactDetailScreen {

    @Composable
    fun Screen(screenContext: ScreenContext) {
        val viewModel = screenContext.contactDetailViewModel
        val contactResource: AsyncResource<IContact> by viewModel.selectedContact.collectAsState()

        Scaffold(
            topBar = {
                ContactDetailTopBar(
                    screenContext = screenContext,
                    contact = contactResource.valueOrNull
                )
            }
        ) {
            contactResource
                .composeIfError { NoContactLoadedError(viewModel = viewModel) }
                .composeIfInactive { NoContactLoaded(router = screenContext.router) }
                .composeIfLoading { LoadingIndicatorFullScreen(R.string.loading_contacts) }
                .composeIfReady { ContactDetailScreenContent.ScreenContent(contact = it) }
        }
    }

    @Composable
    private fun ContactDetailTopBar(
        screenContext: ScreenContext,
        contact: IContact?,
    ) {
        val buttonsEnabled = contact != null
        @StringRes val title = R.string.screen_contact_details

        TopAppBar(
            title = { Text(text = stringResource(id = title)) },
            navigationIcon = {
                BackIconButton { screenContext.router.navigateUp() }
            },
            actions = {
                EditIconButton(enabled = buttonsEnabled) {
                    contact?.let { screenContext.contactEditViewModel.selectContact(it) }
                    screenContext.router.navigateToScreen(Screen.ContactEdit)
                }
                MoreActionsIconButton(enabled = buttonsEnabled) {
                    // TODO implement
                }
            }
        )
    }

    @Composable
    private fun NoContactLoaded(router: AppRouter) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected,
            buttonConfig = ButtonConfig(
                label = R.string.back,
                icon = Icons.Default.ArrowBack,
            ) {
                router.navigateUp()
            }
        )
    }

    @Composable
    private fun NoContactLoadedError(viewModel: ContactDetailViewModel) {
        FullScreenError(
            errorMessage = R.string.no_contact_selected_error,
            buttonConfig = ButtonConfig(
                label = R.string.reload_contact,
                icon = Icons.Default.Sync,
            ) {
                viewModel.reloadContact()
            }
        )
    }
}
