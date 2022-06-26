/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.permission

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import ch.abwesend.privatecontacts.domain.lib.logging.logger

/**
 * Beware: needs to be injected as a Singleton for [setupObserver] to work properly
 */
class CallScreeningRoleHelper {
    private lateinit var roleResultObserver: ActivityResultLauncher<Intent>
    private var currentResultCallback: (PermissionRequestResult) -> Unit = {
        logger.debug("No result callback registered")
    }

    /** needs to be called before [activity] reaches resumed-state */
    fun setupObserver(activity: ComponentActivity) = with(activity) {
        val roleContract = ActivityResultContracts.StartActivityForResult()
        roleResultObserver = registerForActivityResult(roleContract) { result: ActivityResult ->
            val resultCode = if (result.resultCode == Activity.RESULT_OK) PermissionRequestResult.NEWLY_GRANTED else PermissionRequestResult.DENIED
            logger.debug("Role request result: $resultCode")
            currentResultCallback(resultCode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestCallerIdRoleWithExplanation(
        activity: ComponentActivity,
        showExplanation: () -> Unit,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) {
        currentResultCallback = onPermissionResult

        if (activity.hasCallScreeningRole) onPermissionResult(PermissionRequestResult.ALREADY_GRANTED)
        else showExplanation()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestCallerIdRoleNow(
        activity: ComponentActivity,
        onPermissionResult: (PermissionRequestResult) -> Unit,
    ) {
        currentResultCallback = onPermissionResult

        if (activity.hasCallScreeningRole) {
            onPermissionResult(PermissionRequestResult.ALREADY_GRANTED)
        } else {
            val roleManager = activity.getSystemService(Context.ROLE_SERVICE) as RoleManager?
            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            intent?.let { roleResultObserver.launch(it) }
        }
    }

    private val ComponentActivity.hasCallScreeningRole: Boolean
        @RequiresApi(Build.VERSION_CODES.Q)
        get() {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager?
            return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        }
}
