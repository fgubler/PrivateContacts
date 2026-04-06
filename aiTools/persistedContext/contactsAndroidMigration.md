# Contacts-Android Migration — Persisted Context

> **Last updated:** 2026-04-06 (Phase 0 complete)

## Migration Overview

Migrating from `alexstyl/contactstore` to `vestrel00/contacts-android` (v0.5.0) in a phased, low-risk way.
The full initial prompt lives in `aiTools/promptHistory/androidContactsLibraryMigration.md`.

## Key Decisions Made

1. **Package structure**: New implementation lives in `infrastructure.repository.contactsandroid` (sibling of existing `infrastructure.repository.androidcontacts`).
2. **Switching mechanism**: Feature flag `useNewContactsLibrary` (default `false`) in settings, with `DelegatingContactLoadService` / `DelegatingContactSaveService` routing to old or new impl.
3. **Mutable contact model**: Will create a new, parallel mutable contact interface for contacts-android (not refactor the existing one which is tightly coupled to contactstore types).
4. **Old library**: Must NOT be removed. User will do that manually later.
5. **`IContactGroupRepository`**: Only for Room DB (secret contacts). Android contact groups are loaded via `IAndroidContactLoadService.getAllContactGroups()` — no separate group repository migration needed.

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Groundwork / compatibility boundary | ✅ Done |
| 1 | Reading contacts via contacts-android | ⬜ Next |
| 2 | Reading groups/labels via contacts-android | ⬜ Pending |
| 3 | Creating contacts via contacts-android | ⬜ Pending |
| 4 | Creating groups/labels via contacts-android | ⬜ Pending |
| 5 | Editing existing contacts via contacts-android | ⬜ Pending |
| 6 | Cleanup and removal of contactstore | ⬜ Pending (manual) |

## Phase 0 — What Was Done

### Files Created
- `infrastructure/repository/contactsandroid/service/ContactsAndroidLoadService.kt` — stub impl of `IAndroidContactLoadService` (all methods `TODO()`)
- `infrastructure/repository/contactsandroid/service/ContactsAndroidSaveService.kt` — stub impl of `IAndroidContactSaveService` (all methods `TODO()`)
- `infrastructure/repository/contactsandroid/service/DelegatingContactLoadService.kt` — delegates to old or new based on feature flag
- `infrastructure/repository/contactsandroid/service/DelegatingContactSaveService.kt` — delegates to old or new based on feature flag

### Files Modified
- `KoinModule.kt` — wired delegating services as the `IAndroidContactLoadService`/`IAndroidContactSaveService` bindings; added `ContactsAndroidLoadService` and `ContactsAndroidSaveService` as separate Koin factories
- `ISettingsState` / `SettingsState` / `SettingsRepository` / `DataStoreSettingsRepository` / `SettingsEntries` / `PreferencesExtensions` — added `useNewContactsLibrary` boolean (default `false`)
- `TestSettings.kt` — added `useNewContactsLibrary` field

### Directories Created (empty, for future phases)
- Only `infrastructure/repository/contactsandroid/service/` remains populated. The `mapping/`, `model/`, `repository/`, `factory/` subdirs were created then removed (will be recreated as needed).

## Architecture Reference

### Domain Interfaces (DO NOT CHANGE unless necessary)
- `IContactBase`, `IContact` — domain contact model
- `IAndroidContactLoadService` — reading contacts + groups
- `IAndroidContactSaveService` — creating/editing/deleting contacts + groups
- `IContactGroupRepository` — Room DB groups only (not part of this migration)

### Old Implementation (contactstore) — package `infrastructure.repository.androidcontacts`
- `AndroidContactRepositoryBase` — holds `ContactStore` singleton, permission checks
- `AndroidContactLoadRepository` — all read queries
- `AndroidContactSaveRepository` — all write operations
- `AndroidContactLoadService` → implements `IAndroidContactLoadService`
- `AndroidContactSaveService` → implements `IAndroidContactSaveService`
- `AndroidContactChangeService` — builds mutable contacts for save/update
- `IAndroidContactMutable` / `AndroidContactMutable` — tightly coupled to contactstore types
- `AndroidContactMutableFactory` — creates mutable contacts
- Mapping layer in `androidcontacts/mapping/`: `AndroidContactMapper`, `AndroidContactDataMapper`, `AndroidContactGroupMappers`, `AndroidContactAccountMappers`, `AndroidContactImageMappers`, `ContactDataTypeToLabelMappers`

### New Implementation (contacts-android) — package `infrastructure.repository.contactsandroid`
- `service/ContactsAndroidLoadService` — stub
- `service/ContactsAndroidSaveService` — stub
- `service/DelegatingContactLoadService` — feature-flag router
- `service/DelegatingContactSaveService` — feature-flag router

### Key Complexity Areas for Remaining Phases
- **Contact IDs**: `IContactIdExternal` wraps both `contactId: Long` and `lookupKey: String?`. The contacts-android library uses `Contact.id` (Long) and `Contact.lookupKey` (String?).
- **Group membership**: contactstore uses `GroupMembership(groupId)` as a contact data column. contacts-android uses `GroupMembership` similarly but API differs.
- **Mutable contacts**: contactstore has `MutableContact` with `LabeledValue<T>` for phone/email/etc. contacts-android uses `MutableContact` with `MutableList<MutablePhone>`, `MutableList<MutableEmail>`, etc.
- **Contact data types**: Both libraries have label/type enums but they don't map 1:1. Careful mapping needed.
- **Images**: contactstore uses `ImageData(ByteArray)`. contacts-android uses `BlobPhoto(ByteArray)` or `PhotoUri`.

## Continuation Prompt

Use this prompt to continue the migration in a new session:

---

I'm migrating my Android app from `alexstyl/contactstore` to `vestrel00/contacts-android`.

**Read the persisted context file at `aiTools/persistedContext/contactsAndroidMigration.md`** — it contains the full migration state, architecture reference, decisions made, and what was already completed.

**Read the original prompt at `aiTools/promptHistory/androidContactsLibraryMigration.md`** for the full constraints and requirements.

**Key facts:**
- Phase 0 is complete (feature flag, delegating services, stubs).
- The next phase to implement is Phase 1: reading contacts via contacts-android.
- The contacts-android library docs are at https://github.com/vestrel00/contacts-android
- Old library must NOT be removed.
- New code goes in package `infrastructure.repository.contactsandroid`.
- Feature flag `useNewContactsLibrary` controls routing.

Please continue with Phase 1. After completing it, update the persisted context file and summarize what changed.

---
