package ch.abwesend.privatecontacts.domain.service

import android.app.PendingIntent
import android.content.Intent
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolder
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveIntermediateSetupState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupState
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.testutil.TestBase
import io.mockk.coEvery
import io.mockk.every
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
class GoogleDriveSetupServiceTest : TestBase() {
    @MockK
    private lateinit var authRepository: IGoogleDriveAuthorizationRepository

    @MockK
    private lateinit var settingsState: ISettingsState

    @MockK
    private lateinit var driveRepository: IGoogleDriveRepository

    @InjectMockKs
    private lateinit var underTest: GoogleDriveSetupService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { authRepository }
        module.single { settingsState }
    }

    // region requestGoogleDriveAuthorization

    @Test
    fun `requestGoogleDriveAuthorization should return error when authorization fails`() {
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Error

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isEqualTo(GoogleDriveSetupState.Error(GoogleDriveSetupError.AUTHORIZATION_FAILED))
    }

    @Test
    fun `requestGoogleDriveAuthorization should return ConsentRequired when consent is needed`() {
        val pendingIntent = mockk<PendingIntent>()
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.ConsentRequired(pendingIntent)

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isEqualTo(GoogleDriveSetupState.ConsentRequired(pendingIntent))
    }

    @Test
    fun `requestGoogleDriveAuthorization should proceed with setup when authorized`() {
        val email = "test@example.com"
        val folderId = "folder-123"
        val folderName = "BackupFolder"
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns folderId
        every { settingsState.googleDriveFolderName } returns folderName
        coEvery { driveRepository.hasFolderAccess(folderId, folderName) } returns SuccessResult(true)

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isInstanceOf(GoogleDriveIntermediateSetupState.Success::class.java)
        val success = result as GoogleDriveIntermediateSetupState.Success
        assertThat(success.backupEnabled).isTrue()
        assertThat(success.accountEmail).isEqualTo(email)
        assertThat(success.folderId).isEqualTo(folderId)
        assertThat(success.folderName).isEqualTo(folderName)
    }

    // endregion

    // region handleGoogleDriveConsentResponse

    @Test
    fun `handleGoogleDriveConsentResponse should return error when intent handling fails`() {
        val intent = mockk<Intent>()
        coEvery { authRepository.authorizeFromIntent(intent) } returns ErrorResult(Exception("consent failed"))

        val result = runBlocking { underTest.handleGoogleDriveConsentResponse(intent) }

        assertThat(result).isEqualTo(GoogleDriveSetupState.Error(GoogleDriveSetupError.CONSENT_FAILED))
    }

    @Test
    fun `handleGoogleDriveConsentResponse should proceed with setup when intent succeeds`() {
        val intent = mockk<Intent>()
        val email = "user@gmail.com"
        val folderId = "folder-456"
        val folderName = "MyBackups"
        coEvery { authRepository.authorizeFromIntent(intent) } returns SuccessResult(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns folderId
        every { settingsState.googleDriveFolderName } returns folderName
        coEvery { driveRepository.hasFolderAccess(folderId, folderName) } returns SuccessResult(true)

        val result = runBlocking { underTest.handleGoogleDriveConsentResponse(intent) }

        assertThat(result).isInstanceOf(GoogleDriveIntermediateSetupState.Success::class.java)
        val success = result as GoogleDriveIntermediateSetupState.Success
        assertThat(success.accountEmail).isEqualTo(email)
        assertThat(success.folderId).isEqualTo(folderId)
        assertThat(success.folderName).isEqualTo(folderName)
    }

    @Test
    fun `handleGoogleDriveConsentResponse should handle null intent`() {
        coEvery { authRepository.authorizeFromIntent(null) } returns ErrorResult(Exception("null intent"))

        val result = runBlocking { underTest.handleGoogleDriveConsentResponse(null) }

        assertThat(result).isEqualTo(GoogleDriveSetupState.Error(GoogleDriveSetupError.CONSENT_FAILED))
    }

    // endregion

    // region email retrieval

    @Test
    fun `should return error when email retrieval fails`() {
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns ErrorResult(Exception("email error"))

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isEqualTo(GoogleDriveSetupState.Error(GoogleDriveSetupError.EMAIL_RETRIEVAL_FAILED))
    }

    // endregion

    // region folder reuse

    @Test
    fun `should reuse existing folder when it is still accessible`() {
        val email = "user@gmail.com"
        val existingFolderId = "existing-folder-id"
        val existingFolderName = "ExistingFolder"
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns existingFolderId
        every { settingsState.googleDriveFolderName } returns existingFolderName
        coEvery { driveRepository.hasFolderAccess(existingFolderId, existingFolderName) } returns SuccessResult(true)

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isInstanceOf(GoogleDriveIntermediateSetupState.Success::class.java)
        val success = result as GoogleDriveIntermediateSetupState.Success
        assertThat(success.folderId).isEqualTo(existingFolderId)
        assertThat(success.folderName).isEqualTo(existingFolderName)
        assertThat(success.accountEmail).isEqualTo(email)
        assertThat(success.backupEnabled).isTrue()
    }

    @Test
    fun `should create new folder when existing folder is no longer accessible`() {
        val email = "user@gmail.com"
        val existingFolderId = "old-folder-id"
        val existingFolderName = "OldFolder"
        val newFolderId = "new-folder-id"
        val newFolderName = "NewFolder"
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns existingFolderId
        every { settingsState.googleDriveFolderName } returns existingFolderName
        // checkFolderAccess returns false = folder is NOT accessible => create a new one
        coEvery { driveRepository.hasFolderAccess(existingFolderId, existingFolderName) } returns SuccessResult(false)
        coEvery { driveRepository.createBackupFolder() } returns SuccessResult(GoogleDriveFolder(newFolderId, newFolderName))

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isInstanceOf(GoogleDriveIntermediateSetupState.Success::class.java)
        val success = result as GoogleDriveIntermediateSetupState.Success
        assertThat(success.folderId).isEqualTo(newFolderId)
        assertThat(success.folderName).isEqualTo(newFolderName)
        assertThat(success.accountEmail).isEqualTo(email)
        assertThat(success.backupEnabled).isTrue()
    }

    @Test
    fun `should create new folder when no folder is stored`() {
        val email = "user@gmail.com"
        val newFolderId = "new-folder-id"
        val newFolderName = "NewFolder"
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns ""
        every { settingsState.googleDriveFolderName } returns ""
        // when no folder is stored, canReuseBackupFolder returns false immediately (no network call)
        // => falls through to createBackupFolder
        coEvery { driveRepository.createBackupFolder() } returns SuccessResult(GoogleDriveFolder(newFolderId, newFolderName))

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isInstanceOf(GoogleDriveIntermediateSetupState.Success::class.java)
        val success = result as GoogleDriveIntermediateSetupState.Success
        assertThat(success.folderId).isEqualTo(newFolderId)
        assertThat(success.folderName).isEqualTo(newFolderName)
        assertThat(success.accountEmail).isEqualTo(email)
        assertThat(success.backupEnabled).isTrue()
    }

    // endregion

    // region folder creation errors

    @Test
    fun `should return error when folder access check fails`() {
        val email = "user@gmail.com"
        val existingFolderId = "folder-id"
        val existingFolderName = "FolderName"
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns existingFolderId
        every { settingsState.googleDriveFolderName } returns existingFolderName
        coEvery { driveRepository.hasFolderAccess(existingFolderId, existingFolderName) } returns ErrorResult(Exception("access error"))

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isEqualTo(GoogleDriveSetupState.Error(GoogleDriveSetupError.FOLDER_ACCESS_CHECK_FAILED))
    }

    @Test
    fun `should return error when folder creation fails`() {
        val email = "user@gmail.com"
        val existingFolderId = "old-folder-id"
        val existingFolderName = "OldFolder"
        coEvery { authRepository.authorize() } returns GoogleDriveAuthResult.Authorized(driveRepository)
        coEvery { driveRepository.getAccountEmail() } returns SuccessResult(email)
        every { settingsState.googleDriveFolderId } returns existingFolderId
        every { settingsState.googleDriveFolderName } returns existingFolderName
        // folder is no longer accessible => creation required
        coEvery { driveRepository.hasFolderAccess(existingFolderId, existingFolderName) } returns SuccessResult(false)
        coEvery { driveRepository.createBackupFolder() } returns ErrorResult(Exception("creation error"))

        val result = runBlocking { underTest.requestGoogleDriveAuthorization() }

        assertThat(result).isEqualTo(GoogleDriveSetupState.Error(GoogleDriveSetupError.FOLDER_CREATION_FAILED))
    }

    // endregion
}
