/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.getAnywhereWithParams
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullWidth
import ch.abwesend.privatecontacts.view.initialization.InfoDialogs
import ch.abwesend.privatecontacts.view.initialization.InitializationState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.CallPermissionsDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.WaitingForSettings
import ch.abwesend.privatecontacts.view.initialization.PermissionHandler
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.permission.PermissionHelper
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.MainNavHost
import ch.abwesend.privatecontacts.view.theme.PrivateContactsTheme
import ch.abwesend.privatecontacts.view.util.observeAsState
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@FlowPreview
class MainActivity : ComponentActivity() {
    private val permissionHelper: PermissionHelper by injectAnywhere()

    private val contactListViewModel: ContactListViewModel by viewModels()
    private val contactDetailViewModel: ContactDetailViewModel by viewModels()
    private val contactEditViewModel: ContactEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info("Main activity started")

        permissionHelper.setupObserver(this)

        var initializationState: InitializationState by mutableStateOf(WaitingForSettings)
        val nextState: () -> Unit = {
            val oldState = initializationState
            initializationState = initializationState.next()
            logger.debug(
                "Changed initializationState from ${oldState::class.java.simpleName} " +
                    "to ${initializationState::class.java.simpleName}"
            )
        }

        setContent {
            val settings by Settings.observeAsState()

            val isDarkTheme = when (settings.appTheme) {
                AppTheme.LIGHT_MODE -> false
                AppTheme.DARK_MODE -> true
                AppTheme.SYSTEM_SETTINGS -> isSystemInDarkTheme()
            }

            LaunchedEffect(Unit) {
                Settings.repository.initialized.collectLatest { initialized ->
                    if (initialized && initializationState == WaitingForSettings) {
                        nextState()
                    }
                }
            }

            PrivateContactsTheme(isDarkTheme) {
                if (initializationState.showMainContent) {
                    MainContent(initializationState) { nextState() }
                } else {
                    InitialLoadingView()
                }
            }
        }
    }

    @Composable
    private fun MainContent(initializationState: InitializationState, nextState: () -> Unit) {
        val navController = rememberNavController()
        val screenContext = createScreenContext(navController)

        MainNavHost(
            navController = navController,
            screenContext = screenContext,
        )

        InfoDialogs(initializationState) { nextState() }

        if (initializationState == CallPermissionsDialog) {
            PermissionHandler(permissionHelper) { nextState() }
        }
    }

    @Composable
    private fun InitialLoadingView() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.app_logo_large),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.widthIn(max = 500.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(20.dp))
            LoadingIndicatorFullWidth(loadingIndicatorSize = 75.dp)
        }
    }

    private fun createScreenContext(navController: NavHostController): ScreenContext {
        val router: AppRouter = getAnywhereWithParams(navController)

        return ScreenContext(
            router = router,
            contactListViewModel = contactListViewModel,
            contactDetailViewModel = contactDetailViewModel,
            contactEditViewModel = contactEditViewModel,
        )
    }
}
