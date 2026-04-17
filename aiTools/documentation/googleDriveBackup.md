# Google Drive Backup

## Overview
Extends the existing periodic local backup system to optionally upload backups to Google Drive. Local backup logic (`ContactBackupWorker`) remains unchanged; a separate `GoogleDriveBackupWorker` handles the upload.

## Architecture

### Scheduling
- **Periodic**: `BackupScheduler` schedules `GoogleDriveBackupWorker` as a separate periodic job (24h interval, 1h initial delay, requires network). Runs independently but offset from the local backup.
- **One-time** ("Backup now"): Chained after `ContactBackupWorker` using WorkManager's `beginUniqueWork().then()`, so Drive upload runs only after local backup completes.
- Drive worker constraints: `NetworkType.CONNECTED` + the ones from the local backup.

### UI (SettingsScreen)
- New **"Google Drive Backup"** category below the periodic backup section.
- **Disabled state**: Shows a "Select account" button that triggers authorization via `AuthorizationClient`.
- **Setup flow**: If user consent is needed, a `PendingIntent` is launched via `IntentSenderRequest`. If already authorized, folder setup proceeds silently. A unique folder ("PrivateContacts Backups <UUID>") is created on Drive. Progress indicator shown during folder creation.
- **Enabled state**: Displays the Google account email and Drive folder name, with a "Disable" button to clear all Drive settings.

### Error Reporting
- Drive upload errors are written via `IBackupMessageRepository.addDriveMessage()`.
- `MainViewModel.loadBackupMessages()` reads both `LOCAL` and `DRIVE` categories, merges them (sorted by timestamp), and displays them in the existing backup messages dialog on app start.

## Design Decisions
1. **Separate worker, not modifying `ContactBackupWorker`**: Keeps concerns isolated; local backup remains independent and testable.
2. **Filename-based deduplication**: The worker checks if a file with the same name already exists in the Drive folder. Since backup filenames contain dates, this naturally prevents re-uploading the same backup.
3. **Shared DataStore with separate preference keys**: Local and Drive messages use the same `PreferencesDataStore` but different preference keys (via `BackupMessageCategory`), keeping them isolated while sharing all serialization and access logic.
4. **`drive.file` scope**: Minimal permission — the app can only access files/folders it created, not the user's entire Drive.
5. **UUID in folder name**: Ensures uniqueness even if the user sets up Drive backup multiple times or on multiple devices.

## Dependencies
In `build.gradle`:
- `com.google.api-client:google-api-client-android`
- `com.google.apis:google-api-services-drive`
- `com.google.android.gms:play-services-auth` (provides `AuthorizationClient` API)

Added `INTERNET` permission to `AndroidManifest.xml`.