# Google Drive Backup

## Overview
Extends the existing periodic local backup system to optionally upload backups to Google Drive. 
Local backup logic (`ContactBackupWorker`) remains unchanged; a separate `GoogleDriveBackupWorker` handles the upload. 
Google Drive support is only available in the **googlePlay** build flavor; the **fdroid** build uses no-op stubs and hides the UI section entirely.

## Architecture

### Scheduling
- **Periodic**: `BackupScheduler` schedules `GoogleDriveBackupWorker` as a separate periodic job (24h interval, 1h initial delay, requires network). Runs independently but offset from the local backup.
- **One-time** ("Backup now"): Chained after `ContactBackupWorker` using WorkManager's `beginUniqueWork().then()`, so Drive upload runs only after local backup completes.
- Drive worker constraints: `NetworkType.CONNECTED` + the ones from the local backup.

### UI (SettingsScreen)
- The **"Google Drive Backup"** sub-section (below the periodic backup section) is only rendered in the `googlePlay` flavor (`BuildConfig.FLAVOR == "googlePlay"`).
- **Disabled state**: Shows a "Select account" button. Tapping it calls `GoogleDriveSetupService.requestGoogleDriveAuthorization()`, which first clears any cached authorization (so the account/consent picker always appears fresh), then requests authorization via `AuthorizationClient`.
- **Setup flow**: If user consent is needed, a `PendingIntent` is launched via `IntentSenderRequest`. If already authorized, folder setup proceeds silently. A unique folder ("PrivateContacts Backups \<UUID\>") is created on Drive. Progress indicator shown during folder creation.
- **Enabled state**: Displays the Google account email and Drive folder name, with a "Disable" button to clear all Drive settings.
- **Account switching**: The user can switch Google accounts by disabling Drive backup and re-enabling it — the cached authorization is always cleared before each new setup attempt.

### Repository Layer & Build Flavors
- Interfaces `IGoogleDriveAuthorizationRepository` and `IGoogleDriveRepository` live in `main`.
- **googlePlay** source set: real implementations (`GoogleDriveAuthorizationRepository`, `GoogleDriveRepository`) using Play Services and Google Drive API.
- **fdroid** source set: no-op stubs returning `ErrorResult(UnsupportedOperationException(...))` — no Play-only imports.
- Koin binds the same class names; Gradle automatically picks the correct flavor implementation.
- ArchUnit enforces that all Play-only / Drive-specific external imports are confined to `ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository`.

### Domain Models
Flavor-agnostic models in `main` (`domain/model/importexport/googledrive/`):
- `GoogleDriveFile` — represents a file on Drive (replaces the Play-only `DriveFile`).
- `GoogleDriveFolder` — folder id + name.
- `GoogleDriveFolderInfo` — folder metadata used in settings state.
- `GoogleDriveAuthResult` — sealed class: `Authorized`, `ConsentRequired`, `Error`.
- `GoogleDriveSetupState` / `GoogleDriveSetupError` — UI state for the setup flow.

### Error Reporting
- Drive upload errors are written via `IBackupMessageRepository.addDriveMessage()`.
- `MainViewModel.loadBackupMessages()` reads both `LOCAL` and `DRIVE` categories, merges them (sorted by timestamp), and displays them in the existing backup messages dialog on app start.

## Design Decisions
1. **Separate worker, not modifying `ContactBackupWorker`**: Keeps concerns isolated; local backup remains independent and testable.
2. **Filename-based deduplication**: The worker checks if a file with the same name already exists in the Drive folder. Since backup filenames contain dates, this naturally prevents re-uploading the same backup.
3. **Shared DataStore with separate preference keys**: Local and Drive messages use the same `PreferencesDataStore` but different preference keys (via `BackupMessageCategory`), keeping them isolated while sharing all serialization and access logic.
4. **`drive.file` scope**: Minimal permission — the app can only access files/folders it created, not the user's entire Drive.
5. **UUID in folder name**: Ensures uniqueness even if the user sets up Drive backup multiple times or on multiple devices.
6. **Always clear authorization before setup**: `GoogleDriveSetupService` calls `clearAuthorization()` (via `CredentialManager.clearCredentialState()`) before every `authorize()` call, so the account picker always appears and account switching is naturally supported.
7. **Flavor-isolated repositories**: Play-only dependencies (`play-services-auth`, `google-api-client-android`, `google-api-services-drive`, `androidx.credentials`) are declared as `googlePlayImplementation` and only referenced inside the `googlePlay` source set, keeping the fdroid build clean.

## Dependencies
In `app/build.gradle` (all scoped as `googlePlayImplementation`):
- `com.google.android.gms:play-services-auth` (provides `AuthorizationClient` / `Identity` API)
- `com.google.api-client:google-api-client-android`
- `com.google.apis:google-api-services-drive`
- `androidx.credentials:credentials` (used for `CredentialManager.clearCredentialState()`)

Added `INTERNET` permission to `AndroidManifest.xml`.
