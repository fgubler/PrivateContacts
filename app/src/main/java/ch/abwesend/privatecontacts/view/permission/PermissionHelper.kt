/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.ALREADY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.DENIED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.NEWLY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.PARTIALLY_NEWLY_GRANTED

class PermissionHelper {
    private lateinit var singleResultObserver: ActivityResultLauncher<String>
    private lateinit var multipleResultsObserver: ActivityResultLauncher<Array<String>>
    private lateinit var roleResultObserver: ActivityResultLauncher<Intent>

    private var currentPermissions: List<String> = emptyList()
    private var currentResultCallback: (PermissionRequestResult) -> Unit = {
        logger.debug("No result callback registered")
    }

    /** needs to be called before [activity] reaches resumed-state */
    fun setupObserver(activity: ComponentActivity) = with(activity) {
        val singleContract = ActivityResultContracts.RequestPermission()
        singleResultObserver = registerForActivityResult(singleContract) { isGranted: Boolean ->
            val result = if (isGranted) NEWLY_GRANTED else DENIED
            logger.debug("Permission '$currentPermissions' $result")
            currentResultCallback(result)
        }

        val multipleContract = ActivityResultContracts.RequestMultiplePermissions()
        multipleResultsObserver = registerForActivityResult(multipleContract) { isGranted: Map<String, Boolean> ->
            val result = when {
                isGranted.none { it.value } -> DENIED
                isGranted.all { it.value } -> NEWLY_GRANTED
                else -> PARTIALLY_NEWLY_GRANTED.also {
                    isGranted.forEach { (permission, granted) ->
                        logger.debug("Permission $permission ${if (granted) "granted" else "denied"}")
                    }
                }
            }
            logger.debug("Permission '$currentPermissions' $result")
            currentResultCallback(result)
        }

        val roleContract = ActivityResultContracts.StartActivityForResult()
        roleResultObserver = registerForActivityResult(roleContract) { result: ActivityResult ->
            val resultCode = if (result.resultCode == Activity.RESULT_OK) NEWLY_GRANTED else DENIED
            logger.debug("Role request result: $resultCode")
            currentResultCallback(resultCode)
        }
    }

    fun requestUserPermissionsWithExplanation(
        activity: ComponentActivity,
        permissions: List<String>,
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
            else -> showPermissionRequestDialog(permissions, onPermissionResult)
        }
    }

    fun requestUserPermissionsNow(
        permissions: List<String>,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) = showPermissionRequestDialog(permissions, onPermissionResult)

    private fun showPermissionRequestDialog(
        permissions: List<String>,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) {
        currentPermissions = permissions
        currentResultCallback = onPermissionResult

        if (permissions.size == 1) {
            singleResultObserver.launch(permissions.first())
        } else {
            multipleResultsObserver.launch(permissions.toTypedArray())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestCallerIdRoleWithExplanation(
        activity: ComponentActivity,
        showExplanation: () -> Unit,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) {
        currentResultCallback = onPermissionResult

        if (activity.hasCallScreeningRole) onPermissionResult(ALREADY_GRANTED)
        else showExplanation()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestCallerIdRoleNow(
        activity: ComponentActivity,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) {
        currentResultCallback = onPermissionResult

        if (activity.hasCallScreeningRole) {
            onPermissionResult(ALREADY_GRANTED)
        } else {
            val roleManager = activity.getSystemService(ROLE_SERVICE) as RoleManager?
            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            intent?.let { roleResultObserver.launch(it) }
        }
    }

    private val ComponentActivity.hasCallScreeningRole: Boolean
        @RequiresApi(Build.VERSION_CODES.Q)
        get() {
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager?
            return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        }
}
