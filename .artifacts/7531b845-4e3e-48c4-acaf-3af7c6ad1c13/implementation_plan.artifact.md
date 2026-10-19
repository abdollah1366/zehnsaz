# Fix "Cannot add extension with name 'kotlin'" Gradle Sync Error

The error `Cannot add extension with name 'kotlin', as there is an extension already registered with that name` typically occurs when the Kotlin Gradle plugin is applied multiple times or conflicts with another plugin that registers the same extension. This is common in projects using Kotlin 2.0+ when the plugin order or version alignment is not optimal, especially with newer versions of AGP.

## User Review Required

> [!NOTE]
> The project uses very futuristic versions of AGP (`9.3.1`) and Kotlin (`2.2.10`). While I will adjust the plugin application order to resolve the conflict, these versions may have other inherent instabilities.

## Proposed Changes

### Build Configuration

#### [MODIFY] [root build.gradle.kts](file:///C:/Users/Administrator/AndroidStudioProjects/Zehnchin/build.gradle.kts)
- Reorder plugins to ensure `kotlin.android` is listed before `kotlin.compose`.

#### [MODIFY] [app build.gradle.kts](file:///C:/Users/Administrator/AndroidStudioProjects/Zehnchin/app/build.gradle.kts)
- Reorder plugins to apply `kotlin.android` first, before `android.application`.
- Add `kotlin.compose` plugin which is required for Kotlin 2.0+ but was missing from the app module.

## Verification Plan

### Automated Tests
- Run Gradle sync to verify the error is resolved.
- `gradlew assembleDebug` (if sync succeeds).

### Manual Verification
- Verify that the `kotlin` extension is accessible and `kotlinOptions` (or `compilerOptions`) are correctly recognized.
