# Contacts-Android Migration — Persisted Context

> **Last updated:** 2026-04-06 (Phase 1 complete)

## Migration Overview

Migrating from `alexstyl/contactstore` to `vestrel00/contacts-android` (v0.5.0) in a phased, low-risk way.
The full initial prompt lives in `aiTools/promptHistory/androidContactsLibraryMigration.md`.

## Key Decisions Made

1. **Package structure**: New implementation lives in `infrastructure.repository.contactsandroid` (sibling of existing `infrastructure.repository.androidcontacts`).
2. **Switching mechanism**: Feature flag `useNewContactsLibrary` (default `false`) in settings, with `DelegatingContactLoadService` / `DelegatingContactSaveService` routing to old or new impl.
3. **Mutable contact model**: Will create a new, parallel mutable contact interface for contacts-android (not refactor the existing one which is tightly coupled to contactstore types).
4. **Old library**: Must NOT be removed. User will do that manually later.
5. **`IContactGroupRepository`**: Only for Room DB (secret contacts). Android contact groups are loaded via `IAndroidContactLoadService.getAllContactGroups()` — no separate group repository migration needed.
6. **Delegating services**: Accept `IAndroidContactLoadService`/`IAndroidContactSaveService` interfaces (not concrete types) to avoid architecture test violations.

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Groundwork / compatibility boundary | ✅ Done |
| 1 | Reading contacts via contacts-android | ✅ Done |
| 2 | Reading groups/labels via contacts-android | ✅ Done (included in Phase 1) |
| 3 | Creating contacts via contacts-android | ⬜ Next |
| 4 | Creating groups/labels via contacts-android | ⬜ Pending |
| 5 | Editing existing contacts via contacts-android | ⬜ Pending |
| 6 | Cleanup and removal of contactstore | ⬜ Pending (manual) |

## Phase 0 — What Was Done

### Files Created
- `contactsandroid/service/ContactsAndroidLoadService.kt` — stub impl of `IAndroidContactLoadService` (all methods `TODO()`)
- `contactsandroid/service/ContactsAndroidSaveService.kt` — stub impl of `IAndroidContactSaveService` (all methods `TODO()`)
- `contactsandroid/service/DelegatingContactLoadService.kt` — delegates to old or new based on feature flag
- `contactsandroid/service/DelegatingContactSaveService.kt` — delegates to old or new based on feature flag

### Files Modified
- `KoinModule.kt` — wired delegating services as the `IAndroidContactLoadService`/`IAndroidContactSaveService` bindings
- `ISettingsState` / `SettingsState` / `SettingsRepository` / `DataStoreSettingsRepository` / `SettingsEntries` / `PreferencesExtensions` — added `useNewContactsLibrary` boolean (default `false`)
- `TestSettings.kt` — added `useNewContactsLibrary` field

## Phase 1 — What Was Done

### Files Created
- `contactsandroid/mapping/ContactsAndroidTypeMapper.kt` — maps contacts-android entity types (`PhoneEntity.Type`, `EmailEntity.Type`, `AddressEntity.Type`, `EventEntity.Type`, `RelationEntity.Type`) to domain `ContactDataType`
- `contactsandroid/mapping/ContactsAndroidDataMapper.kt` — maps contacts-android `Contact` data (phones, emails, addresses, websites, relations, events, companies) to domain `ContactData` list. Handles pseudo-relation company workaround.
- `contactsandroid/mapping/ContactsAndroidContactMapper.kt` — maps contacts-android `Contact` to domain `IContactBase`, `IContact`, and `ContactWithPhoneNumbers`. Handles images via `contact.photoBytes(contactsApi)` extension. Includes company/nickname workarounds for contacts without first/last name.
- `contactsandroid/mapping/ContactsAndroidGroupMapper.kt` — maps contacts-android `Group` to domain `ContactGroup`
- `contactsandroid/repository/ContactsAndroidLoadRepository.kt` — repository layer using `Contacts` API: `query()`, `broadQuery()`, `phoneLookupQuery()`, `groups().query()`. Handles permission checks, ID-based and lookup-key-based resolution.

### Files Modified
- `contactsandroid/service/ContactsAndroidLoadService.kt` — fully implemented all `IAndroidContactLoadService` methods using the new repository and mappers
- `contactsandroid/service/DelegatingContactLoadService.kt` — changed constructor to accept `IAndroidContactLoadService` interfaces (was concrete types)
- `contactsandroid/service/DelegatingContactSaveService.kt` — changed constructor to accept `IAndroidContactSaveService` interfaces (was concrete types)
- `KoinModule.kt` — added Koin bindings for `ContactsAndroidContactMapper`, `ContactsAndroidDataMapper`, `ContactsAndroidLoadRepository`

### Key API Mappings (contacts-android v0.5.0)
- Query by ID: `contactsApi.query().where { Fields.Contact.Id equalTo id }.find()`
- Query by lookup key: `contactsApi.query().where { Fields.Contact.LookupKey equalTo key }.find()`
- Broad search: `contactsApi.broadQuery().wherePartiallyMatches(searchText).find()`
- Phone lookup: `contactsApi.phoneLookupQuery().whereExactlyMatches(phoneNumber).find()`
- Groups: `contactsApi.groups().query().find()`
- Photo bytes: `contact.photoBytes(contactsApi)` (extension function from `contacts.core.util`)
- Entity ID: `entity.idOrNull` (from `Entity` base interface)
- Type enums: Live on `*Entity` sealed interfaces (e.g., `PhoneEntity.Type`, `RelationEntity.Type`), NOT on the data classes

### Behavior Notes
- Groups reading (Phase 2) was included in Phase 1 since `getAllContactGroups()` is part of `IAndroidContactLoadService`
- Company pseudo-relation workaround is preserved: relations with `RelationEntity.Type.CUSTOM` and matching label pattern are treated as companies
- Contact photo is fetched via extension function requiring `Contacts` API instance (injected via Koin)
- Architecture test required delegating services to use interfaces, not concrete types from `androidcontacts` package

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
- Mapping layer in `androidcontacts/mapping/`

### New Implementation (contacts-android) — package `infrastructure.repository.contactsandroid`
- `mapping/ContactsAndroidTypeMapper.kt` — type enum mapping
- `mapping/ContactsAndroidDataMapper.kt` — contact data mapping
- `mapping/ContactsAndroidContactMapper.kt` — contact entity mapping
- `mapping/ContactsAndroidGroupMapper.kt` — group mapping
- `repository/ContactsAndroidLoadRepository.kt` — query repository
- `service/ContactsAndroidLoadService.kt` — fully implemented load service
- `service/ContactsAndroidSaveService.kt` — stub (TODO for Phase 3+)
- `service/DelegatingContactLoadService.kt` — feature-flag router
- `service/DelegatingContactSaveService.kt` — feature-flag router

### Key Complexity Areas for Remaining Phases
- **Mutable contacts**: contactstore has `MutableContact` with `LabeledValue<T>`. contacts-android uses `NewRawContact` for inserts and `MutableRawContact` for updates. A new parallel mutable contact interface is needed.
- **Insert API**: `contactsApi.insert().rawContacts(newRawContact).commit()`
- **Update API**: `contactsApi.update().contacts(mutableContact).commit()`
- **Delete API**: `contactsApi.delete().contacts(contact).commit()`
- **Group insert**: `contactsApi.groups().insert().groups(newGroup).commit()`
- **Reverse mappers needed**: domain model → contacts-android `NewRawContact` / `MutableRawContact`

## Continuation Prompt

Use this prompt to continue the migration in a new session:

---

I'm migrating my Android app from `alexstyl/contactstore` to `vestrel00/contacts-android`.

**Read the persisted context file at `aiTools/persistedContext/contactsAndroidMigration.md`** — it contains the full migration state, architecture reference, decisions made, and what was already completed.

**Read the original prompt at `aiTools/promptHistory/androidContactsLibraryMigration.md`** for the full constraints and requirements.

**Key facts:**
- Phase 0 and Phase 1 (including Phase 2 groups) are complete.
- The next phase to implement is Phase 3: creating contacts via contacts-android.
- The contacts-android library docs are at https://github.com/vestrel00/contacts-android
- Old library must NOT be removed.
- New code goes in package `infrastructure.repository.contactsandroid`.
- Feature flag `useNewContactsLibrary` controls routing.
- Delegating services use interfaces, not concrete types (architecture test constraint).

Please continue with Phase 3. After completing it, update the persisted context file and summarize what changed.

---
