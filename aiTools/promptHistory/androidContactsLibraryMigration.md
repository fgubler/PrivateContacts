# Android Contacts Library Migration
This is about migrating 
- from [ContactStore](https://github.com/alexstyl/contactstore) 
- to [contacts-android](https://github.com/anggrayudi/contacts-android).
The initial prompt was written together with ChatGPT.

## Initial Prompt
I want to migrate my Android app from `alexstyl/contactstore` to `vestrel00/contacts-android` in a phased, low-risk way.

Context:
- The app is written in Kotlin.
- I already support:
    - reading contacts
    - creating contacts
    - editing existing contacts
    - reading labels / contact-groups
    - creating labels / contact-groups
- I have my own domain/data model for contacts, so the migration scope is mostly limited to the infrastructure and mapping layer.
- I do NOT want a big-bang rewrite.
- I want the old library to remain in place initially while we build the new implementation beside it.
- I prefer incremental, compileable changes.
- I want the migration done in phases, ideally with vertical slices after the groundwork is in place.

Goal:
Design and implement a migration from `contactstore` to `contacts-android` that keeps the app stable and allows gradual rollout.

Constraints:
- Do not remove the old library yet.
- Do not rewrite the app-facing domain model unless absolutely necessary. That involves e.g.
  - The interfaces `IContactBase` and `IContact` and their sub-interfaces and implementations.
  - The interface `IAndroidContactLoadService` for reading contacts: a new implementation of it should be added for the new library but the interface should not change unless necessary.
  - The interface `IAndroidContactSaveService` for creating/editing contacts: a new implementation of it should be added for the new library but the interface should not change unless necessary.
  - The interface `IContactGroupRepository` for reading groups/labels: a new implementation of it should be added for the new library but the interface should not change unless necessary.
- Preserve existing behavior as much as possible.
- Keep changes small and reviewable.
- Prefer Kotlin-idiomatic code.
- Avoid unnecessary abstraction, but introduce clear boundaries where they reduce migration risk.
- Keep the app compiling after each phase.

Desired migration strategy:
1. First, inspect the codebase and identify:
    - where `contactstore` is used: check the package `infrastructure.repository.androidcontacts` 
    - which app-facing interfaces already exist, e.g.
        - The interfaces `IContactBase` and `IContact
        - The interface `IAndroidContactLoadService`
        - The interface `IAndroidContactSaveService`
        - The interface `IContactGroupRepository`
    - where mapping between my domain model and `contactstore` livescheck the package `infrastructure.repository.androidcontacts.mapping`
    - how groups/labels are handled
    - where IDs and lookup keys are stored and used
2. Then propose a phased migration plan that starts with a compatibility boundary:
    - define or refine stable interfaces around contact operations
    - keep the current `contactstore` implementation behind those interfaces
    - add a parallel `contacts-android` implementation beside it
3. Then implement the migration in phases:
    - Phase 0: groundwork / compatibility boundary / test scaffolding
    - Phase 1: reading contacts via `contacts-android`
    - Phase 2: reading groups/labels via `contacts-android` if separate
    - Phase 3: creating contacts via `contacts-android`
    - Phase 4: creating groups/labels via `contacts-android` if separate
    - Phase 5: editing existing contacts via `contacts-android`
    - Phase 6: cleanup and removal of `contactstore`
4. Use DI (Koin, see `KoinModule`) so old and new implementations can coexist during migration.

Implementation requirements:
- Prefer adapting infrastructure behind existing domain-facing interfaces.
- If interfaces are missing or too coupled to `contactstore`, introduce minimal new interfaces
- Keep mapping logic separated by use case rather than building one giant mapper.
- Be explicit and careful about:
    - contact ID vs raw contact ID
    - lookup keys
    - group IDs
    - group membership
    - optional / partial fields
    - labels/groups semantics
- Add tests where useful, especially contract tests around the domain-facing interfaces and mapper tests for the new implementation.
- If behavior differs between the two libraries, document it clearly and suggest the safest way to preserve current app behavior.
- If possible, implement end-to-end tests in the form of unit-tests where (if possible) only the last step of accessing OS-APIs is mocked. Check if the libraries already offer setups to allow such tests.
    - Add those tests for both the old and the new library. 

How I want you to work:
- First, inspect the codebase and give me a concise migration plan tailored to the actual project structure.
- Then start implementing Phase 0 only.
- After Phase 0, summarize:
    - what changed
    - which interfaces / adapters were introduced
    - where the old and new implementations now live
    - what risks or follow-up items remain
- Then proceed phase by phase, keeping each step small and reviewable.
- Do not jump straight to deleting `contactstore`. In fact: DO NOT UNDER ANY CIRCUMSTANCES DELETE IT. I will manually do that, later.
- Do not do broad refactors unless they directly support the migration.
- If something is unclear, ask for clarification: DO NOT MAKE ASSUMPTIONS - ever.
- prefer consulting the online documentation over decompiling library code.

Deliverables for each phase:
- code changes
- brief explanation of the approach
- any tradeoffs or behavior differences
- list of touched files
- suggested next phase

Please begin by:
1. scanning the project structure,
2. locating all `contactstore` usages,
3. identifying the existing abstraction boundaries and mapping code,
4. proposing the best phased migration plan for this specific codebase,
5. implementing Phase 0 only.
