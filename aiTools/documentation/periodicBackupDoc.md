# Periodic Backup Feature

## Overview
The app supports automatic, periodic backups of contacts to a user-selected folder. Backups are triggered by a background `WorkManager` job (`ContactBackupWorker`) and produce `.vcf` (or `.vcf.crypt`) files in the chosen folder.

## Settings
All backup-related settings are stored in the app's `PreferencesDataStore` via `DataStoreSettingsRepository`:

| Setting | Description |
|---|---|
| `backupFrequency` | How often backups run: Disabled / Daily / Weekly / Monthly |
| `backupContactScope` | Which contacts to back up: All / Secret only / Public only |
| `backupFolder` | URI of the target folder (persisted SAF permission) |
| `numberOfBackupsToKeep` | How many backup files to retain per contact type: All / 5 / 10 / 30 / 50 |
| `backupEncryptionEnabled` | Whether backup files are encrypted |

## Architecture

### WorkManager
`ContactBackupWorker` is a `CoroutineWorker` scheduled via `WorkManager`. It reads the current settings, checks whether a backup is due, and exports contacts using `ContactExportService`. The worker runs in a foreground service context with a notification to satisfy Android background execution requirements.

### File Naming
Backup files are named with a type prefix and a date string, e.g. `backup_secret_2024-01-15.vcf`. This deterministic naming allows the cleanup logic to identify and sort backups per contact type without any additional metadata.

### Secret vs. Public Contacts
Public and secret contacts are backed up independently into separate files, and the `numberOfBackupsToKeep` limit applies independently to each type.

### Cleanup of Old Backups
After each successful backup, `deleteOldBackups()` is called. It lists all files in the backup folder whose names start with `backup_secret_` or `backup_public_`, sorts them alphabetically (which equals chronological order given the date-based naming), and deletes the oldest ones if the count exceeds the configured limit.

### ImportExport Screen Card
The `ImportExportScreen` contains a dedicated "Periodic Backup" card (`PeriodicBackupCategoryComponent`) with two actions:

- **Go to Settings**: navigates directly to `SettingsScreen` via `IContactImportExportScreenContext.navigateToSettingsScreen()`, so the user can configure backup frequency, scope, folder, retention, and encryption without leaving the import/export flow.
- **Validate Encrypted Backup**: opens a file picker (accepting `.vcf` and `.vcf.crypt` files). If a `.vcf.crypt` file is selected, the user is prompted for the decryption password. The file is then read, decrypted (if needed), and parsed via `ContactImportService.loadContacts()`. The result shows how many contacts were found and how many parsing errors occurred. No contacts are actually imported.

The validation result is managed through `ContactImportViewModel.validationResult` (a `ResourceFlow`) and displayed via the shared `ResourceFlowProgressAndResultDialog` component, consistent with the existing import flow.

### Encryption
Backup files can optionally be encrypted. See [backupEncryptionDoc.md](backupEncryptionDoc.md) for details on the encryption design, key management, and the `.vcf.crypt` file format.
