# CLAUDE.md

Android Native learning project: Kotlin + Jetpack Compose + MVVM + Retrofit + Hilt.
Single module `:app`, package `com.example.postsapp`. Docs and comments are in Arabic (user preference).

## Commands
- `./gradlew assembleDebug`: build
- `./gradlew test`: unit tests (JVM, no device needed)
- `./gradlew lint`: Android lint

Requires the Android SDK (`ANDROID_HOME` or `local.properties`). Versions live in `gradle/libs.versions.toml`.

## Conventions
- Layers: `data/` (model, remote, repository) → `ui/<feature>/` (ViewModel + Screen) → `di/` (Hilt modules).
- Each screen = `XxxRoute` (gets `hiltViewModel()`, collects state) + stateless `XxxScreen(state, callbacks)` with a `@Preview`.
- ViewModels expose `StateFlow<UiState<T>>`; rethrow `CancellationException`, map others with `toUserMessage()`.
- Navigation routes are defined in `ui/navigation/Routes`; pass ids, not objects.
- ViewModel tests use a fake `PostRepository` + `MainDispatcherRule`.
