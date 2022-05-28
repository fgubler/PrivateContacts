/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.ALREADY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.DENIED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.NEWLY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.PARTIALLY_NEWLY_GRANTED

/**
 * Beware: subclasses need to be injected as a Singleton for [setupObservers] to work properly
 */
abstract class PermissionHelperBase {
    private lateinit var singleResultObserver: ActivityResultLauncher<String>
    private lateinit var multipleResultsObserver: ActivityResultLauncher<Array<String>>

    private var currentPermissions: List<String> = emptyList()
    private var currentResultCallback: (PermissionRequestResult) -> Unit = {
        logger.debug("No result callback registered")
    }

    protected abstract val permissions: List<String>

    /** needs to be called before [activity] reaches resumed-state */
    fun setupObservers(activity: ComponentActivity) = with(activity) {
        val singleContract = ActivityResultContracts.RequestPermission()
        singleResultObserver = registerForActivityResult(singleContract) { isGranted: Boolean ->
            val result = if (isGranted) NEWLY_GRANTED else DENIED
            logger.debug("Permission '$currentPermissions' $result")
            currentResultCallback(result)
        }

        val multipleContract = ActivityResultContracts.RequestMultiplePermissions()
        multipleResultsObserver = registerForActivityResult(multipleContract) { isGranted: Map<String, Boolean> ->
            val result = when {
                isGranted.none { it.value } -> DENIED.also {
                    logger.debug("All permissions denied: ${ isGranted.keys }")
                }
                isGranted.all { it.value } -> NEWLY_GRANTED.also {
                    logger.debug("All permissions granted: ${ isGranted.keys }")
                }
                else -> PARTIALLY_NEWLY_GRANTED.also {
                    isGranted.forEach { (permission, granted) ->
                        logger.debug("Permission $permission ${ if (granted) "granted" else "denied" }")
                    }
                }
            }
            logger.debug("Permission '$currentPermissions' $result")
            currentResultCallback(result)
        }
    }

    fun requestUserPermissionsWithExplanation(
        activity: ComponentActivity,
        enforceShowingExplanation: Boolean = true,
        onShowExplanation: () -> Unit,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) = with(activity) {
        currentPermissions = permissions
        currentResultCallback = onPermissionResult

        val permissionStates = permissions.map {
            it to ContextCompat.checkSelfPermission(this, it)
        }
        val allAlreadyGranted = permissionStates.all { it.second == PERMISSION_GRANTED }
        val showExplanation = enforceShowingExplanation || permissions.any { shouldShowRequestPermissionRationale(it) }

        when {
            allAlreadyGranted -> {
                logger.debug("Permissions $permissions already granted")
                onPermissionResult(ALREADY_GRANTED)
            }
            showExplanation -> onShowExplanation()
            else -> showPermissionRequestDialog(onPermissionResult)
        }
    }

    fun requestUserPermissionsNow(
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) = showPermissionRequestDialog(onPermissionResult)

    private fun showPermissionRequestDialog(onPermissionResult: (PermissionRequestResult) -> Unit) {
        currentPermissions = permissions
        currentResultCallback = onPermissionResult

        if (permissions.size == 1) {
            singleResultObserver.launch(permissions.first())
        } else {
            multipleResultsObserver.launch(permissions.toTypedArray())
        }
    }
}
