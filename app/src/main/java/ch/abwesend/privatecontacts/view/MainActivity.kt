/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.Settings
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.util.getAnywhereWithParams
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoNeverDialog
import ch.abwesend.privatecontacts.view.model.ScreenContext
import ch.abwesend.privatecontacts.view.permission.PermissionHandler
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.NEWLY_GRANTED
import ch.abwesend.privatecontacts.view.routing.AppRouter
import ch.abwesend.privatecontacts.view.routing.MainNavHost
import ch.abwesend.privatecontacts.view.theme.PrivateContactsTheme
import ch.abwesend.privatecontacts.view.viewmodel.ContactDetailViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactEditViewModel
import ch.abwesend.privatecontacts.view.viewmodel.ContactListViewModel
import kotlinx.coroutines.FlowPreview

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@FlowPreview
class MainActivity : ComponentActivity() {
    private val permissionHandler: PermissionHandler by injectAnywhere()

    private val contactListViewModel: ContactListViewModel by viewModels()
    private val contactDetailViewModel: ContactDetailViewModel by viewModels()
    private val contactEditViewModel: ContactEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info("Main activity started")

        permissionHandler.setupObserver(this)
        var showPhoneStateExplanation by mutableStateOf(false)

        setContent {
            val darkTheme = Settings.isDarkTheme || isSystemInDarkTheme()

            PrivateContactsTheme(darkTheme) {
                val navController = rememberNavController()
                val screenContext = createScreenContext(navController)

                MainNavHost(
                    navController = navController,
                    screenContext = screenContext,
                )

                if (showPhoneStateExplanation) {
                    YesNoNeverDialog(
                        title = R.string.show_caller_information_title,
                        text = R.string.show_caller_information_text,
                        onYes = {
                            showPhoneStateExplanation = false
                            requestPhoneStatePermission(showExplanation = null)
                        },
                        onNo = { doNotShowAgain ->
                            showPhoneStateExplanation = false
                            if (doNotShowAgain) {
                                Settings.doNotAskForPhoneStatePermission()
                            }
                        }
                    )
                }
            }
        }

        requestPhoneStatePermission { showPhoneStateExplanation = true }
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

    private fun requestPhoneStatePermission(showExplanation: (() -> Unit)?) {
        if (!Settings.requestPhoneStatePermission) {
            return
        }

        val onPermissionResult: (PermissionRequestResult) -> Unit = { result ->
            if (result == NEWLY_GRANTED) {
                Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
            }
        }

        val permission = Manifest.permission.READ_PHONE_STATE

        showExplanation?.let {
            permissionHandler.requestUserPermissionWithExplanation(
                activity = this,
                permission = permission,
                onShowExplanation = showExplanation,
                onPermissionResult = onPermissionResult
            )
        } ?: permissionHandler.requestUserPermissionNow(
            permission = permission,
            onPermissionResult = onPermissionResult
        )
    }
}
