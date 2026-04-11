# Encryption
## Initial Prompt
The class ContactBackupWorker is used to create periodic backups of the data in this app, based on the app settings.
I would like to add the option to encrypt those backups.

### Changes to user settings
#### UI changes
On the SettingsScreen, there is already a category for periodic backups.
Please add a checkbox component there to enable encryption.
Consider the existing generic components for this that are used on that screen.

When the checkbox is changed to true, a dialog should open where the user can enter a password.
There should be a cancel-button button in that case, the checkbox will be unchecked again. 
If the checkbox is changed to false, also the stored password should be cleared and any now-obsolete key-material should be discarded.

All new or changed, translated texts should be added to English, German, French, Italian and Spanish.

#### New behavior
That password should be stored securely, e.g. by encrypting with the help of Android's KeyStore API 
before storing it in the app's PreferencesDataStore. 
Also consider alternative approaches but ask before implementing them.

Only state-of-the-art encryption algorithms should be used.
Prefer Java's built-in encryption APIs over adding new dependencies.
Check build.gradle first to see what is available. Ask before adding any new dependencies.

### Changes to the Backup- and Export-Logic
The existing backup-logic in ContactBackupWorker shares some code with the normal import/export functionality.
For now, we only want to add the option of encryption to backups, not normal exports.
However, some of the common logic will probably have to be changed. It is fine to pass a flag whether data should be encrypted.

Add a new EncryptionRepository to host all logic directly related to encryption.
Only use encryption if the user has enabled it in the settings.
If that is the case, retrieve and decrypt the password from the settings and use it to encrypt the data before writing it to its destination file.
Only use state-of-the-art encryption algorithms. Prefer Java's built-in encryption APIs over adding new dependencies.
Prefer constants over hard-coded values.

The file-extension of encrypted files should be .vcf.crypt

### Changes to the import-logic
The existing import-logic will need to be adapted to be able to handle both .vcf and .vcf.crypt files.
Also, the existing logic to advertise the app as an app that can open .vcf files should be extended to also open .vcf.crypt files, if possible.

### Additional considerations
- Prefer concise and idiomatic kotlin code
- Only add comments where necessary
- Use the existing code as a starting point for your implementation
- Add unit-tests for new code, where easily achievable. Consider the existing tests for their structure, used dependencies, superclasses, etc.
- If anything is unclear, ask for clarification rather than making assumptions.
- Do not execute more than 3 test-files: ask me to do it for you.

#### Documentation
After implementing the feature, create a short and concise documentation about its function and all design- and architecture-decisions taken during its development.

Store that documentation in the folder 'aiTools/documentation' by
- either creating a new markdown file for that feature or topic
- or adding a new section to an existing markdown file

# Google Drive Backups
## Initial Prompt
The App contains logic for periodic, local backups.
 - In SettingsScreen, the settings are defined for whether and how the backups should be created.
 - BackupScheduler defines how and when the backups should be done. 
 - ContactBackupWorker takes care of the actual backup-logic.

I want to extend that to also allow backups to be stored on Google Drive.

In the SettingsScreen, controls should be added to
 - enable/disable Google Drive backups
 - select the Google Account to use for backups which should launch a Google account-chooser as well as ask for the permission to create a new folder there including the read- and write-permission in that folder.
   - A new folder should be created in Google-Drive, called "PrivateContacts Backups", followed by a UUID to make sure that it is unique.
   - The name of the folder should also be displayed on SettingsScreen.

I do not want to change the existing backup-logic in ContactBackupWorker.
Instead, I want a second Worker-Class which 
 - takes the newest local backup made by ContactBackupWorker of each type (public and secret contacts)
 - checks whether that has already been successfully uploaded to Google Drive
 - if not, uploads it to Google Drive
The task should be scheduled to run daily: if possible, after the ContactBackupWorker job has finished.
 - If the upload fails, a Retry-Result should be returned.
 - That task should also write to the list of error-messages in DataStore which will be shown to the user on next start of the app.
   - Maybe better to write in a separate list/property in DataStore to make sure they don't mix.

If something is unclear, pause the work and ask for clarification.
Use only modern, idiomatic kotlin code.
Use the existing code as a starting point for your implementation.
Re-use code rather than duplicating it. Try to use elegant abstractions and extract logic into separate methods and classes if that makes it more readable.
The code should be robust and readable.

After implementing the feature, create a short and concise documentation about its function and all design- and architecture-decisions taken during its development.
Store that documentation in the folder 'aiTools/documentation' by
- either creating a new markdown file for that feature or topic
- or adding a new section to an existing markdown file
