/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.AppTheme
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsState
import ch.abwesend.privatecontacts.domain.util.LocaleUtils
import ch.abwesend.privatecontacts.domain.util.getAnywhereWithParams
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.ImportContactsFromIntentComponents.ObserveVcfImportResult
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullWidth
import ch.abwesend.privatecontacts.view.initialization.CallPermissionHandler
import ch.abwesend.privatecontacts.view.initialization.InfoDialogs
import ch.abwesend.privatecontacts.view.initialization.InitializationState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.CallPermissionsDialog
import ch.abwesend.privatecontacts.view.initialization.InitializationState.InfoDialogState
import ch.abwesend.privatecontacts.view.initialization.InitializationState.Initialized
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.CANCELLED
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.DENIED
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.ERROR
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.NOT_AUTHENTICATED
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.NO_DEVICE_AUTHENTICATION_REGISTERED
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus.SUCCESS
import ch.abwesend.privatecontacts.view.model.screencontext.ScreenContext
import ch.abwesend.privatecontacts.view.permission.AndroidContactPermissionHelper
import ch.abwesend.privatecontacts.view.permission.CallPermissionHelper
import ch.abwesend.privatecontacts.view.permission.CallScreeningRoleHelper
import ch.abwesend.privatecontacts.view.permission.PermissionProvider
import ch.abwesend.privatecontacts.view.routing.GenericRouter
import ch.abwesend.privatecontacts.view.routing.MainNavHost
import ch.abwesend.privatecontacts.view.theme.PrivateContactsTheme
import ch.abwesend.privatecontacts.view.util.authenticateWithBiometrics
import ch.abwesend.privatecontacts.view.util.observeAsNullableState
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactExportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactImportViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import ch.abwesend.privatecontacts.view.viewmodel.MainViewModel
import ch.abwesend.privatecontacts.view.viewmodel.SettingsViewModel
import kotlin.contracts.ExperimentalContracts
import kotlinx.coroutines.FlowPreview

private const val FILE_PERMISSION_FLAG = Intent.FLAG_GRANT_READ_URI_PERMISSION

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalContracts
@FlowPreview
class MainActivity : AppCompatActivity() {
    private val callPermissionHelper: CallPermissionHelper by injectAnywhere()
    private val contactPermissionHelper: AndroidContactPermissionHelper by injectAnywhere()
    private val callScreeningRoleHelper: CallScreeningRoleHelper by injectAnywhere()

    private val viewModel: MainViewModel by viewModels()
    private val contactListViewModel: ContactListViewModel by viewModels()
    private val contactDetailViewModel: ContactDetailViewModel by viewModels()
    private val contactEditViewModel: ContactEditViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val exportViewModel: ContactExportViewModel by viewModels()
    private val importViewModel: ContactImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info("Main activity started")

        callPermissionHelper.setupObservers(this)
        contactPermissionHelper.setupObservers(this)
        callScreeningRoleHelper.setupObserver(this)

        handleIncomingIntent(intent)

        setContent {
            val initializationState by viewModel.initializationState
            val settings by Settings.observeAsNullableState()
            val isDarkTheme = isDarkTheme(settings)

            PrivateContactsTheme(isDarkTheme) {
                settings?.let {
                    AppPreInitialization(it)
                    MainContent(initializationState, viewModel, it) { viewModel.goToNextState() }
                } ?: InitialLoadingView()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(newIntent: Intent?) {
        val action = newIntent?.action ?: return

        when (action) {
            Intent.ACTION_VIEW -> newIntent.data?.let {
                logger.info("Opening vCard file: $it")
                grantUriPermission(packageName, it, FILE_PERMISSION_FLAG)
                viewModel.parseVcfFile(it)
            }
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(newIntent, Intent.EXTRA_STREAM, Uri::class.java)?.let {
                logger.info("Sharing vCard file: $it")
                viewModel.parseVcfFile(it)
            }
        }
    }

    @Composable
    private fun isDarkTheme(settings: ISettingsState?): Boolean =
        when (settings?.appTheme ?: SettingsState.defaultSettings.appTheme) {
            AppTheme.LIGHT_MODE -> false
            AppTheme.DARK_MODE -> true
            AppTheme.SYSTEM_SETTINGS -> isSystemInDarkTheme()
        }

    @Composable
    private fun AppPreInitialization(settings: ISettingsState) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.updateAppStatistics(settings)
            val language = Settings.current.appLanguage
            LocaleUtils.applyLanguage(context, language)
        }
    }

    @Composable
    private fun MainContent(
        initializationState: InitializationState,
        viewModel: MainViewModel,
        settings: ISettingsState,
        nextState: () -> Unit,
    ) {
        LaunchedEffect(Unit) {
            handleAuthentication(settings, viewModel)
        }

        val navController = rememberNavController()
        val screenContext = createScreenContext(navController, settings)

        AuthenticatedContent(settings) {
            MainNavHost(
                navController = navController,
                screenContext = screenContext,
            )

            ObserveVcfImportResult(viewModel, screenContext) {
                revokeUriPermission(it, FILE_PERMISSION_FLAG)
            }

            when (initializationState) {
                is InfoDialogState -> InfoDialogs(initializationState, settings) { nextState() }
                is CallPermissionsDialog -> CallPermissionHandler(
                    settings = settings,
                    permissionHelper = callPermissionHelper,
                    roleHelper = callScreeningRoleHelper,
                ) { nextState() }
                is Initialized -> { /* nothing to do */ }
            }
        }
    }

    private fun handleAuthentication(settings: ISettingsState, viewModel: MainViewModel) {
        if (settings.authenticationRequired) {
            val authenticationFlow = authenticateWithBiometrics(
                activity = this,
                promptTitle = getString(R.string.app_name),
                promptDescription = getString(R.string.authentication_required_prompt_description)
            )
            viewModel.handleAuthenticationResult(authenticationFlow)
        } else {
            viewModel.grantAccessWithoutAuthentication()
        }
    }

    @Composable
    private fun AuthenticatedContent(settings: ISettingsState, content: @Composable () -> Unit) {
        when (viewModel.authenticationStatus.value) {
            SUCCESS, NO_DEVICE_AUTHENTICATION_REGISTERED -> content()
            NOT_AUTHENTICATED -> Unit // wait for authentication-result
            CANCELLED, DENIED, ERROR -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(text = stringResource(id = R.string.authentication_failed))
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { handleAuthentication(settings, viewModel) }) {
                        Text(text = stringResource(id = R.string.try_again))
                    }
                }
            }
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

    private fun createScreenContext(
        navController: NavHostController,
        settings: ISettingsState
    ): ScreenContext {
        val router: GenericRouter = getAnywhereWithParams(navController)
        val permissionProvider = PermissionProvider(
            callPermissionHelper,
            contactPermissionHelper,
            callScreeningRoleHelper
        )
        return ScreenContext(
            genericRouter = router,
            settings = settings,
            permissionProvider = permissionProvider,
            contactListViewModel = contactListViewModel,
            contactDetailViewModel = contactDetailViewModel,
            contactEditViewModel = contactEditViewModel,
            settingsViewModel = settingsViewModel,
            exportViewModel = exportViewModel,
            importViewModel = importViewModel,
        )
    }
}
