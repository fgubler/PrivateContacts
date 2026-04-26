### Junie Skill: Prepare to Release a New Version

#### Steps

**Step 1 — Read current version info**
- Open `app/build.gradle` and locate the `defaultConfig` block.
- Ask the user for the new versionCode and versionName.
- Wait for the user's reply before continuing.

**Step 2 — Ask for the topic of the change**
- Ask the user: *"What are the changes / topics for this release? Please describe them so I can write the release notes."*
- **Pause and wait** for the user's reply before continuing.

**Step 3 — Create release-note string resources**
- For each release-note bullet point, add a new `<string>` entry with the naming pattern `release_notes_v{newVersionCode}_N` (where N is 1, 2, … for multiple bullets; omit the suffix if there is only one) in the following files:
    - `app/src/main/res/values/strings.xml` — English
    - `app/src/main/res/values-de/strings.xml` — German translation
    - `app/src/main/res/values-fr/strings.xml` — French translation
    - `app/src/main/res/values-it/strings.xml` — Italian translation
    - `app/src/main/res/values-es/strings.xml` — Spanish translation
- Escape apostrophes with `\'` in all XML string values.
- Place the new entries at the end of the existing release-note strings in each file.

**Step 4 — Update `ReleaseNotes.kt`**
- Open `app/src/main/java/ch/abwesend/privatecontacts/view/initialization/ReleaseNotes.kt`.
- Add a new `ReleaseNote(...)` entry to the `releaseNotes` list, just before the `// Add more release notes as needed` comment, following the existing pattern:
  ```kotlin
  ReleaseNote(
      versionCode = <newVersionCode>,
      textResourceIds = listOf(R.string.release_notes_v<newVersionCode>_1, R.string.release_notes_v<newVersionCode>_2, ...)
  ),
  ```
- If there is only a single release-note string, use `release_notes_v<newVersionCode>` without a numeric suffix.

**Step 5 — Create fastlane changelog file**
- Create a new file at `fastlane/metadata/android/en-US/changelogs/{newVersionCode}.txt`.
- The file content should contain two lines: 
  1. `Version {newVersionName}`
  2. The topic of the release (given previously by the user)

**Step 6 — Verify**
- Confirm all modified/created files are consistent (version codes match, string resource names match between `strings.xml` files and `ReleaseNotes.kt`).
- Do **not** commit; leave the changes uncommitted for the user to review.