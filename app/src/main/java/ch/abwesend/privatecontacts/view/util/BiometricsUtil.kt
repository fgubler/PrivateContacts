package ch.abwesend.privatecontacts.view.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT_PERMANENT
import androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
import androidx.biometric.BiometricPrompt.ERROR_NO_BIOMETRICS
import androidx.biometric.BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import androidx.biometric.BiometricPrompt.ERROR_VENDOR
import androidx.core.content.ContextCompat
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.view.model.AuthenticationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

private val allowedAuthentications: Int
    get() = Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL

fun Context.canUseBiometrics(): Boolean {
    return try {
        val biometricManager = BiometricManager.from(this)
        when (val response = biometricManager.canAuthenticate(allowedAuthentications)) {
            BIOMETRIC_SUCCESS -> {
                logger.debug("App can authenticate using biometrics.")
                true
            }
            BIOMETRIC_ERROR_NO_HARDWARE -> {
                logger.info("No biometrics hardware found: App cannot authenticate using biometrics.")
                false
            }
            BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                logger.info("Biometrics hardware unavailable: App cannot authenticate using biometrics.")
                false
            }
            BIOMETRIC_ERROR_NONE_ENROLLED -> {
                logger.info("No biometrics are set up: App cannot authenticate using biometrics.")
                false
            }
            else -> {
                logger.warning("Invalid response from BiometricManager: $response.")
                false
            }
        }
    } catch (e: Exception) {
        logger.warning("Exception while checking the availability of biometrics", e)
        false
    }
}

fun authenticateWithBiometrics(
    activity: AppCompatActivity,
    promptTitle: String,
    promptDescription: String? = null,
): Flow<AuthenticationStatus> = callbackFlow {
    logger.debug("Authenticating with biometrics: title = $promptTitle")
    if (activity.canUseBiometrics()) {
        val executor = ContextCompat.getMainExecutor(activity.applicationContext)
        val callback = createAuthenticationCallback()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle)
            .setDescription(promptDescription)
            .setAllowedAuthenticators(allowedAuthentications)
            .setConfirmationRequired(false)
            .build()

        withContext(Dispatchers.Main.immediate) {
            biometricPrompt.authenticate(promptInfo)
        }
    } else {
        logger.debug("Cannot authenticate with biometrics.")
        trySendBlocking(AuthenticationStatus.NO_DEVICE_AUTHENTICATION_REGISTERED)
        channel.close()
    }
    awaitClose { logger.debug("Authentication closed") }
}

private fun ProducerScope<AuthenticationStatus>.createAuthenticationCallback(): BiometricPrompt.AuthenticationCallback {
    return object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            logger.debug("Authentication succeeded: grant access.")
            trySendBlocking(AuthenticationStatus.SUCCESS)
            channel.close()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            logger.debug("Authentication failed: do not grant access.")
            trySendBlocking(AuthenticationStatus.DENIED)
            channel.close()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            logger.debug("Error '$errorCode' during authentication: do not grant access. $errString")
            val status = when (errorCode) {
                ERROR_USER_CANCELED, ERROR_NEGATIVE_BUTTON -> AuthenticationStatus.CANCELLED
                ERROR_NO_BIOMETRICS, ERROR_NO_DEVICE_CREDENTIAL, ERROR_VENDOR ->
                    AuthenticationStatus.NO_DEVICE_AUTHENTICATION_REGISTERED
                ERROR_LOCKOUT, ERROR_LOCKOUT_PERMANENT -> AuthenticationStatus.DENIED
                else -> AuthenticationStatus.ERROR // TODO pass the error-string to the user?
            }
            trySendBlocking(status)
            channel.close()
        }
    }
}
