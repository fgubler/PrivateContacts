/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.ALREADY_GRANTED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.DENIED
import ch.abwesend.privatecontacts.view.permission.PermissionRequestResult.NEWLY_GRANTED

enum class PermissionRequestResult {
    NEWLY_GRANTED,
    ALREADY_GRANTED,
    DENIED,
}

class PermissionHandler {
    private lateinit var resultObserver: ActivityResultLauncher<String>

    private var currentPermission: String = "[No permission requested]"
    private var currentResultCallback: (PermissionRequestResult) -> Unit = {
        logger.debug("No result callback registered")
    }

    /** needs to be called before [activity] reaches resumed-state */
    fun setupObserver(activity: ComponentActivity) = with(activity) {
        val contract = ActivityResultContracts.RequestPermission()
        resultObserver = registerForActivityResult(contract) { isGranted: Boolean ->
            val result = if (isGranted) NEWLY_GRANTED else DENIED
            logger.debug("Permission '$currentPermission' $result")
            currentResultCallback(result)
        }
    }

    fun requestUserPermissionWithExplanation(
        activity: ComponentActivity,
        permission: String,
        onShowExplanation: () -> Unit,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) = with(activity) {
        currentPermission = permission
        currentResultCallback = onPermissionResult

        val permissionState = ContextCompat.checkSelfPermission(this, permission)

        when {
            permissionState == PackageManager.PERMISSION_GRANTED -> {
                logger.debug("Permission $permission already granted")
                onPermissionResult(ALREADY_GRANTED)
            }
            shouldShowRequestPermissionRationale(permission) -> onShowExplanation()
            else -> showPermissionRequestDialog(permission, onPermissionResult)
        }
    }

    fun requestUserPermissionNow(
        permission: String,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) = showPermissionRequestDialog(permission, onPermissionResult)

    private fun showPermissionRequestDialog(
        permission: String,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) {
        currentPermission = permission
        currentResultCallback = onPermissionResult

        resultObserver.launch(permission)
    }
}
