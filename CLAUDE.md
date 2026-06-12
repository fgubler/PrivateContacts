# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this app does

_Private Contacts_ stores contacts that should be invisible to other apps. "Secret" contacts live in a local Room database; "public" contacts live in the standard Android ContactsProvider. The app can read/edit both types.

## Build & Test Commands

```bash
# Build
./gradlew :app:assembleGooglePlayDebug

# All unit tests
./gradlew :app:testGooglePlayDebugUnitTest

# Single test class
./gradlew :app:testGooglePlayDebugUnitTest --tests "ch.abwesend.privatecontacts.domain.service.ContactLoadServiceTest"

# Single test method (use backtick-quoted display name)
./gradlew :app:testGooglePlayDebugUnitTest --tests "ch.abwesend.privatecontacts.domain.service.ContactLoadServiceTest::search should pass the query to the repository"

# Lint (ktlint)
./gradlew :app:ktlintCheck
./gradlew :app:ktlintFormat

# Instrumented tests (requires connected device)
./gradlew :app:connectedAndroidTest
```

Build flavors: `googlePlay` (includes Firebase Crashlytics) and `fdroid` (no Firebase). Default is `googlePlay`.

## Architecture

The project uses **Clean Architecture** with four layers, enforced by ArchUnit tests that will fail the build on violations:

```
APPLICATION → VIEW → DOMAIN ← INFRASTRUCTURE
```

- **`application/`** — Koin DI wiring (`KoinModule.kt`), `AndroidApplication`, routing
- **`view/`** — Jetpack Compose screens, ViewModels (extend `ViewModel`)
- **`domain/`** — Business logic, models, repository/service interfaces. No Android imports.
- **`infrastructure/`** — Room DB, Android APIs, ContactStore, Google Drive, vCard parsing

ViewModels are created at `MainActivity` level and passed down via CompositionLocal — do not use `viewModel()` inside sub-composables.

## Key Patterns

**Interfaces are prefixed with `I`**: `IContactRepository`, `IContactEditable`, `IContactId`. Implementations drop the prefix.

**Dependency injection via Koin** (not Hilt). Inject with `by injectAnywhere()`. Most things are `factory`; `SettingsRepository` is `single` because DataStore requires a stable instance.

**`BinaryResult`** — use this custom sealed type (instead of `Result<T>`) for any operation that can succeed or fail. It is the project convention for error-handling in services.

**`ResourceFlow<T>`** — custom wrapper around coroutine flows for loading/ready/error states. Use it in ViewModels instead of `StateFlow<Result<T>>`.

**`IDispatchers`** — abstraction over coroutine dispatchers. Inject and use instead of `Dispatchers.IO` directly so tests can swap in `TestDispatchers`.

**Settings** are stored in DataStore and accessed via `SettingsRepository` (Koin `single`).

**String resources in tests** — use `StringProvider` / `ResourcesBasedStringProvider` abstraction, not `context.getString()` directly, so domain/service tests can run without Android context.

## Testing Conventions

- **Framework**: JUnit 5 Jupiter + MockK + AssertJ (not JUnit 4, not Mockito)
- **Base class**: Extend `TestBase` — it sets up Koin and handles MockK lifecycle. Override `setupKoinModule(module)` to register mocks.
- **Async**: use `coEvery`/`coVerify` for suspend functions; `runBlocking` to drive coroutines in tests.
- **Test data**: use builders from `testutil/databuilders/` (`ContactBuilder`, `ContactDataBuilder`, etc.) and `someXxx()` factory functions.
- **Verification**: call `confirmVerified(mock)` to assert no unexpected calls.

## Adding a New Contact Data Type (e.g. `EventDate`)

1. **Domain model** in `domain/model/contactdata/` — implement `IContactDataGeneric<T>`
2. **Repository interface** in `domain/repository/` if new persistence is needed
3. **Infrastructure** — Room entity + DAO + repository impl; register in `KoinModule.kt`
4. **UI** — new composable in `view/screens/contactedit/components/`; wire into `ContactDataEditComponents` and `ContactEditScreenContent`
5. **String resources** in `app/src/main/res/values/strings.xml`

## ktlint Disabled Rules

Four rules are disabled project-wide: `trailing-comma-on-declaration-site`, `trailing-comma-on-call-site`, `multiline-if-else`, `annotation`. Do not add trailing commas expecting them to be enforced.

## Personal Code Style

- use .entries for enums instead of .values()
- never use early returns
- never use one word variable names (use it if suitable)

## Output
- No preamble. No "Great question!", "Sure!", "Of course!", "Certainly!", "Absolutely!".
- No hollow closings. No "I hope this helps!", "Let me know if you need anything!".
- No restating the prompt. If the task is clear, execute immediately.

## Token Efficiency
- Compress responses where possible.
- No redundant context. Do not repeat information already established in the session.
- No long intros or transitions between sections.

## Typography - ASCII Only
- Do not use em dashes. Use hyphens instead.
- Do not use smart or curly quotes. Use straight quotes instead.
- Do not use the ellipsis character. Use three plain dots instead.
- Do not use Unicode bullets. Use hyphens or asterisks instead.
- Do not use non-breaking spaces.

## Sycophancy - Zero Tolerance
- Never validate the user before answering.
- Never say "You're absolutely right!" unless the user made a verifiable correct statement.
- Disagree when wrong. State the correction directly.

## Accuracy and Speculation Control
- Never speculate about code, files, or APIs you have not read.
- If referencing a file or function: read it first, then answer.
- If unsure: say "I don't know." Never guess confidently.
- Never invent file paths, function names, or API signatures.

## Code Output
- No docstrings or comments on code that was not changed.
- Inline comments only where logic is non-obvious.
- Read the file before modifying it. Never edit blind.

## Session Memory
- Learn user corrections and preferences within the session.
- Apply them silently. Do not re-announce learned behavior.
- If the user corrects a mistake: fix it, remember it, move on.

## Override Rule
User instructions always override this file.
