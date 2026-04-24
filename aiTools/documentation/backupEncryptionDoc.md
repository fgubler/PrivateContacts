# Backup Encryption

## Overview

Periodic backups created by `ContactBackupWorker` can optionally be encrypted with a user-supplied password. Encrypted backup files use the `.vcf.crypt` extension. The import screen can open both `.vcf` and `.vcf.crypt` files.

---

## Architecture & Design Decisions

### Password Storage

The user's backup password is never stored in plain text. Instead:

1. When the user enables encryption and enters a password, it is encrypted using a key stored in the **Android KeyStore** (AES-256-GCM).
2. The result is serialized as a **JSON string** (algorithm, IV, and ciphertext Base64-encoded) and stored in the app's **PreferencesDataStore** (`encryptedBackupPassword`):

```json
{
  "version": 1,
  "algorithm": "AES/GCM/NoPadding",
  "tagLength": 128,
  "iv": "<base64>",
  "ciphertext": "<base64>"
}
```

Storing the algorithm parameters in the JSON enables future changes without breaking backwards-compatibility.

3. When encryption is disabled, both the stored ciphertext and the KeyStore key are deleted.

**Why Android KeyStore?**  
The KeyStore provides hardware-backed key storage on supported devices. The key never leaves the secure enclave, making it the standard Android approach for protecting secrets at rest. No additional dependencies are required.

### File Encryption

Backup files are encrypted using **AES-256-GCM** with a key derived from the user's password via **PBKDF2WithHmacSHA256** (310,000 iterations, 16-byte random salt). The encrypted output is a **JSON string** with all parameters and binary values Base64-encoded:

```json
{
  "version": 1,
  "algorithm": "AES/GCM/NoPadding",
  "kdf": "PBKDF2WithHmacSHA256",
  "iterations": 310000,
  "keySize": 256,
  "tagLength": 128,
  "salt": "<base64>",
  "iv": "<base64>",
  "ciphertext": "<base64>"
}
```

A fresh random salt and IV are generated for every backup, ensuring that two backups with the same password produce different ciphertext.

Storing the algorithm parameters inside the JSON enables future changes to iteration count, key size, or algorithm without breaking backwards-compatibility: decryption reads all parameters from the JSON rather than relying on hardcoded constants.

**Why PBKDF2 + AES-GCM?**
- PBKDF2 is a well-established, NIST-recommended key derivation function available in the standard JDK — no extra dependencies needed.
- AES-256-GCM provides both confidentiality and integrity (authenticated encryption), protecting against tampering.
- 310,000 iterations align with OWASP's current recommendation for PBKDF2-HMAC-SHA256.
- Storing algorithm parameters in plaintext alongside the ciphertext is standard practice (e.g., bcrypt, Argon2, JWE) and poses no security risk, as salt and IV are designed to be non-secret.

### Encryption Repository

All encryption logic is encapsulated in `EncryptionRepository` (implementing `IEncryptionRepository`), registered as a Koin singleton. 
This keeps encryption concerns isolated from backup and import/export logic.

### Password Recovery Failure Handling

When a periodic backup runs and encryption is enabled, but the decryption of the stored password fails (e.g. the KeyStore key was deleted or the device was reset):

1. Encryption is **automatically disabled** (`Settings.repository.backupEncryptionEnabled = false`).
2. An **error message** is persisted via `addErrorMessage()` so the user is informed at the next app startup that the password could not be recovered and encryption has been turned off.
3. The backup proceeds **without encryption** rather than failing silently or crashing.

### Backup Validation

The `ImportExportScreen` provides a "Validate Encrypted Backup" action that lets the user verify a backup file without importing it:

1. A file picker opens, accepting both `.vcf` and `.vcf.crypt` files.
2. If a `.vcf.crypt` file is selected, the user is prompted for the decryption password via `PasswordInputDialog`.
3. `ContactImportService.loadContacts()` reads the file, decrypts it (if a password was provided), and parses the VCF data — the same code path used during a real import.
4. The result (number of contacts found, number of parsing errors, or an error description) is shown in a dialog. No contacts are saved.

This reuses the existing import pipeline end-to-end, ensuring that validation accurately reflects what a real import would produce.

---

## File Format

| Extension | Content | Readable by app |
|---|---|---|
| `.vcf` | Plain-text VCF | ✅ |
| `.vcf.crypt` | JSON envelope with AES-256-GCM encrypted VCF (PBKDF2 key), Base64-encoded binary fields, MIME type `text/plain` | ✅ (password required) |

---
