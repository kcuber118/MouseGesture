# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project actually is

This is an **Android native app (Kotlin + Jetpack Compose)**, not a Flutter project — despite the `Flutter/` segment in the parent directory path. Do **not** run `flutter`, `dart`, or look for `pubspec.yaml`/`lib/`; there are none. Treat it as a standard Android Studio Gradle project.

The project has been renamed away from the default template to match the folder name: `rootProject.name = "MouseGesture"`, package/`applicationId`/`namespace` = `com.example.mousegesture`, Compose theme = `MouseGestureTheme`, XML theme style = `Theme.MouseGesture`, app label = "Mouse Gesture". The only UI is still the template `Greeting("Android")` — the intended feature (mouse/stylus gesture handling on Android) is not yet implemented; expect to build it from scratch on top of the Compose scaffold.

## Toolchain

- **AGP 9.2.1**, **Kotlin 2.2.10** (Compose compiler plugin), **Gradle 9.4.1** wrapper. Versions are centralized in `gradle/libs.versions.toml`.
- **Compose BOM 2026.02.01**, Material3. **compileSdk = 36 with minorApiLevel 1**, **minSdk 24**, **targetSdk 36**.
- Java 11 source/target compatibility. JDK 17/21 is available locally; the wrapper pins Gradle 9.4.1.

### AGP 9.x DSL differences (important)
The build scripts use the newer AGP 9.x API, which differs from tutorials targeting AGP 8.x:
- `compileSdk` is a block: `compileSdk { version = release(36) { minorApiLevel = 1 } }` — not `compileSdk = 36`.
- The release build type disables optimization via `optimization { enable = false }` — not `isMinifyEnabled = false`.

Match this DSL when editing `app/build.gradle.kts`. Version catalog aliases (`libs.*`) must be declared in `gradle/libs.versions.toml` before use.

## Common commands

All Gradle tasks run through the wrapper (`./gradlew`). SDK location is set in `local.properties` (`sdk.dir=/Volumes/khoadev/androidsdk`, gitignored).

- **Build debug APK:** `./gradlew assembleDebug`
- **Build + install on connected device/emulator:** `./gradlew installDebug`
- **Lint:** `./gradlew lintDebug`
- **Run all unit tests:** `./gradlew testDebugUnitTest`
- **Run a single unit test class:** `./gradlew testDebugUnitTest --tests "com.example.mousegesture.ExampleUnitTest"`
- **Run a single test method:** `./gradlew testDebugUnitTest --tests "com.example.mousegesture.ExampleUnitTest.addition_isCorrect"`
- **Instrumented/UI tests (needs a running device/emulator):** `./gradlew connectedAndroidTest`
- **Clean:** `./gradlew clean`

Gradle configuration cache is enabled (`org.gradle.configuration-cache=true`); if a build script changes in a way the cache can't handle, run with `--no-configuration-cache`.

## Architecture

Single-activity Compose app following the default Android Studio "Empty Activity (Compose)" template:

- `app/src/main/java/com/example/myapplication/`
  - `MainActivity.kt` — `ComponentActivity` using `enableEdgeToEdge()` + `setContent { MouseGestureTheme { Scaffold { ... } } }`. This is the entry point declared in `AndroidManifest.xml` with the `MAIN`/`LAUNCHER` intent filter.
  - `ui/theme/` — `Theme.kt` (`MouseGestureTheme`), `Color.kt`, `Type.kt`.
- `app/src/main/res/` — standard resources (strings, colors, themes, launcher icons, backup rules).
- `app/src/test/` — JVM unit tests (JUnit 4). `app/src/androidTest/` — instrumented tests (Espresso + Compose `createAndroidComposeRule`).

When adding the gesture feature, the natural place is new composables/screens under `ui/` and any input handling via Compose `Modifier.pointerInput` / `detectDragGestures` / `PointerEvent` (mouse source = `PointerType.Mouse`). Keep the single-activity, Compose-navigation-friendly structure unless a reason demands otherwise.

## Notes

- This is a git repository (GitHub remote: `kcuber118/MouseGesture`, default branch `main`). Earlier drafts of this note said it was not a git repo — that is no longer true.
- `local.properties` and `.idea/` workspace files are machine-specific and gitignored — don't rely on or commit them.

## Agent skills

### Issue tracker

Issues live in **GitHub Issues** for this repo (`kcuber118/MouseGesture`) via the `gh` CLI. PRs are not a request surface. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical roles use their default names — `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix` — applied as GitHub labels. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout: one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
