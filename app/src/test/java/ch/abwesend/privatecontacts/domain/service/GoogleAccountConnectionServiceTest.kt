package ch.abwesend.privatecontacts.domain.service

import android.app.PendingIntent
import android.content.Intent
import ch.abwesend.privatecontacts.domain.model.googleaccount.GoogleAccountConnectIntermediateState
import ch.abwesend.privatecontacts.domain.model.googleaccount.GoogleAccountConnectState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class GoogleAccountConnectionServiceTest : TestBase() {
    @MockK
    private lateinit var authRepository: IGoogleDriveAuthorizationRepository

    @MockK
    private lateinit var driveRepository: IGoogleDriveRepository

    @InjectMockKs
    private lateinit var underTest: GoogleAccountConnectionService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { authRepository }
    }

    // region connectAccount

    @Test
    fun `connectAccount should clear authorization and return the account email when authorized`() {
        val email = "user@gmail.com"
        coJustRun { authRepository.clearAuthorization() }
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)

        val result = runBlocking { underTest.connectAccount() }

        assertThat(result).isEqualTo(GoogleAccountConnectIntermediateState.Success(email))
        coVerify { authRepository.clearAuthorization() }
    }

    @Test
    fun `connectAccount should return ConsentRequired when consent is needed`() {
        val pendingIntent = mockk<PendingIntent>()
        coJustRun { authRepository.clearAuthorization() }
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.ConsentRequired(pendingIntent)

        val result = runBlocking { underTest.connectAccount() }

        assertThat(result).isEqualTo(GoogleAccountConnectState.ConsentRequired(pendingIntent))
    }

    @Test
    fun `connectAccount should return an error when authorization fails`() {
        coJustRun { authRepository.clearAuthorization() }
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Error

        val result = runBlocking { underTest.connectAccount() }

        assertThat(result).isEqualTo(GoogleAccountConnectState.Error(GoogleDriveSetupError.AUTHORIZATION_FAILED))
    }

    @Test
    fun `connectAccount should return an error when the email cannot be retrieved`() {
        coJustRun { authRepository.clearAuthorization() }
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns ErrorResult(Exception("email error"))

        val result = runBlocking { underTest.connectAccount() }

        assertThat(result).isEqualTo(GoogleAccountConnectState.Error(GoogleDriveSetupError.EMAIL_RETRIEVAL_FAILED))
    }

    // endregion

    // region handleConsentResponse

    @Test
    fun `handleConsentResponse should return the account email when the intent succeeds`() {
        val intent = mockk<Intent>()
        val email = "consented@gmail.com"
        coEvery { authRepository.authorizeFromIntent(intent) } returns SuccessResult(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)

        val result = runBlocking { underTest.handleConsentResponse(intent) }

        assertThat(result).isEqualTo(GoogleAccountConnectIntermediateState.Success(email))
    }

    @Test
    fun `handleConsentResponse should return an error when intent handling fails`() {
        val intent = mockk<Intent>()
        coEvery { authRepository.authorizeFromIntent(intent) } returns ErrorResult(Exception("consent failed"))

        val result = runBlocking { underTest.handleConsentResponse(intent) }

        assertThat(result).isEqualTo(GoogleAccountConnectState.Error(GoogleDriveSetupError.CONSENT_FAILED))
    }

    // endregion

    @Test
    fun `disconnectAccount should clear the cached authorization`() {
        coJustRun { authRepository.clearAuthorization() }

        runBlocking { underTest.disconnectAccount() }

        coVerify { authRepository.clearAuthorization() }
    }
}
