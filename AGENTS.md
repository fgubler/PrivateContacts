# Project Guidelines for Junie

## What the project is about
This android app is an alternative to the standard contacts app of Android.
The goal is to keep those contacts there which should not be shared with other apps.
The app can also display and edit "normal" contacts (called "public" in the code) but the main focus are those contacts called "secret" which are stored in a room database.

## Tech Stack
- Kotlin 2.0+ with modern features: sealed classes/interfaces, context receivers where applicable, inline classes.
- Jetpack Compose (Material theme, state hoisting, remember + derivedStateOf).
- Architecture: MVVM with the ViewModels passed down from the activity (see MainActivity.kt).
- Dependency Injection: Koin
- Navigation: Compose Navigation (NavHostController).
- Data: Room for local DB

## Code Style
- Use Kotlin idioms: extension functions, sealed classes for UI states/results, data classes for models.
- Avoid: raw Java types, mutable state in Composables, deprecated APIs.

## UI/UX Consistency
- Match existing Material theme (colors, typography from app theme) and UX.
- State management: ViewModel flows with StateFlow/collectAsState().
- Accessibility: contentDescription on images/icons, semantics.

## Testing
- Unit tests with JUnit5 + MockK.

## Folder Structure
Follow the existing patterns in the project.
