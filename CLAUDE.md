# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project

Makalu is a delivery app for Android (Kotlin, Jetpack Compose, Material 3 Expressive). The app talks
to a separate backend API.

## Build & run

```bash
./gradlew assembleDebug                     # build the app
./gradlew installDebug                      # install on a connected device/emulator
./gradlew :features:orders:compileDebugKotlin   # fast compile check for one module
./gradlew test                              # all JVM unit tests
./gradlew :features:orders:testDebugUnitTest   # unit tests of one Android module
./gradlew :shared:test                      # unit tests of the JVM module
./gradlew :features:orders:testDebugUnitTest --tests "*OrdersViewModelTest"  # single test class
```

There is no lint/format tooling configured beyond the default `./gradlew lint`.

### Unit tests

Only JVM unit tests (`src/test`), no instrumented tests. JUnit 4, MockK, kotlinx-coroutines-test,
ktor-client-mock, and Robolectric (only where real Android classes are needed). The `:testing`
module (`testImplementation(projects.testing)` in every module) provides `MainDispatcherRule` and
`mockHttpClientManager { request -> respondJson(...) }`, a mocked `HttpClientManager` backed by a Ktor
`MockEngine` for `XxxClient` tests. ViewModel tests mock the feature client with `coEvery` instead of
using `MockEngine`. Tests live in the same package as the class under test so `internal` classes are
reachable.

### Coverage

JaCoCo is configured once in the root `build.gradle.kts` for every module except `:testing`
(Android modules get `enableUnitTestCoverage` on `debug`; the version is `jacoco` in the catalog).

```bash
./gradlew jacocoAggregatedReport                          # all modules → build/reports/jacoco/aggregated/
./gradlew :features:orders:createDebugUnitTestCoverageReport  # one Android module → build/reports/coverage/test/debug/
./gradlew :shared:jacocoTestReport                        # JVM module → build/reports/jacoco/test/
```

Previews are tagged `@ExcludeFromGeneratedCoverageReport` (`:shared`) so JaCoCo drops them; tag new
`@Preview` functions too.

### SonarQube

`./gradlew sonar` runs `jacocoAggregatedReport` and uploads the analysis to the self-hosted
SonarQube server. The plugin's Android source detection doesn't work
with AGP 9 built-in Kotlin, so the root build script sets `sonar.sources`/`sonar.tests`/
`sonar.java.binaries` explicitly; keep `sonarCoverageExclusions` in sync with `coverageExclusions`.

### Required `local.properties` entries

- `BASE_URL` – backend base URL. The `:shared` module's `generateConfig` task reads it and generates
  a top-level `MakaluConfig` object (`import MakaluConfig`), used by `HttpClientManager`.
- `MAPS_API_KEY` – injected into `:features:address`'s manifest by the Google secrets Gradle plugin.
- `SONAR_HOST_URL`, `SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_PROJECT_NAME` – only needed by
  `./gradlew sonar`; environment variables of the same name are used as a fallback. The project
  name is optional and defaults to the Gradle root project name (`Makalu`).

### Build setup notes

- AGP 9 with **built-in Kotlin** (`android.builtInKotlin=true`, `android.newDsl=true`): Android
  modules do *not* apply `org.jetbrains.kotlin.android`; they apply `android.application`/
  `android.library` + `kotlin.compose` (+ `kotlin.serialization` where needed).
- JVM target 21 everywhere. SDK levels and app version live in `gradle/libs.versions.toml`
  (`compileSdk`, `minSdk`, …) and are read via `libs.versions.*`.
- Type-safe project accessors are enabled: depend on modules as
  `implementation(projects.features.orders)`.
- All dependencies go through the version catalog; use the `compose`, `coil`, and `ktor` bundles.

## Module architecture

- `:app` – `MakaluApplication` (starts Koin with every module's DI module), `MainActivity`, the app
  `Scaffold` (top bar, bottom bar, FAB), theme, and the single `NavigationGraph`.
- `:shared` – **pure Kotlin/JVM library** (no Android dependencies). Holds navigation abstractions,
  the Ktor `HttpClientManager`, `Resource`/`MakaluError`, DataStore `Settings`, `LogoutUseCase`, and
  API models shared across features. It `api`-exports Ktor, kotlinx-serialization, and DataStore to
  consumers.
- `:components` – shared Compose UI: `ScreenContainer`, buttons, banners, app-bar/FAB controllers,
  `UiText`, ViewModel extensions, and the image cropper screen.
- `:features:*` – one Android library per feature (auth, home, orders, profile, address, store,
  menuItem, cart). Features depend on `:shared` and `:components` only — never on each other. Note
  that package names are inconsistent (`eti.lucasgomes.makalu.features.*`,
  `eti.lucasgomes.features.*`, and `eti.lucasgomes.adress`); follow whatever the module already
  uses.

When adding a feature module: register it in `settings.gradle.kts`, add it to
`app/build.gradle.kts`, add its Koin module to `MakaluApplication`, add a `Destination` and a
`composable<>` entry in `NavigationGraph`.

## Feature structure (MVI-style)

Each screen follows the same shape (see `features/orders/.../detail/` for a current example):

- `XxxEntry(innerPadding, args…)` – public composable called from `NavigationGraph`. Gets the
  ViewModel with `koinViewModel { parametersOf(...) }`, collects `uiState` with
  `collectAsStateWithLifecycle`, wraps content in `ScreenContainer`.
- `XxxScreen(uiState, onAction)` – stateless UI, with `@Preview`s.
- `XxxUiState` – immutable data class (or sealed interface) exposed as `StateFlow`; errors are
  `UiText`.
- `XxxAction` – sealed interface of user intents; the ViewModel has a single `onAction(action)`
  dispatcher. Initial loads are triggered via an `OnInitialFetch`-style action from
  `OnFirstComposition`.
- `XxxViewModel` – `internal`, updates a `MutableStateFlow` with `_uiState.update { it.copy(...) }`,
  launches work with `withViewModelScope { }`.
- `XxxClient` – `internal` API client per feature wrapping
  `httpClientManager.withApiResource { get("/path") }`, returning `Resource<T>`; chain with
  `.onSuccess {}` / `.onError {}` / `.map {}`.
- `*UiStateMapper.kt` – extension functions mapping `*Response` DTOs to UI state.
- DI: a Koin `module` per feature (`viewModelOf`, or `viewModel { params -> ... }` when it takes nav
  args; clients as `singleOf`).

Keep classes `internal` to their feature module unless `:app` needs them (usually only the `Entry`).

## Cross-cutting mechanisms

- **Navigation**: ViewModels never touch `NavController`. They inject the `Navigator` interface
  (`:shared`) and call `navigate(Destination…)`, `navigateUp()`, or `navigateForResult`/
  `navigateUpWithResult` with a `RequestKey`. `DefaultNavigator` emits `NavigationAction`s on a
  channel, which `MainComposable` observes and `bindNavigationEvents` translates into
  `NavHostController` calls (results go through `SavedStateHandle`). Routes are type-safe
  `@Serializable` objects/classes in `Destination.Screen`, grouped into the `Graph.Auth` and
  `Graph.Root` nested graphs.
- **App bars & FAB**: the scaffold lives in `:app`; screens configure it through CompositionLocals
  (`LocalTopBarUiController`, `LocalBottomBarUiController`, `LocalFabUiController`) using helpers
  like `ConfigureTopBar(...)` / `ConfigureFab(...)` from `:components`.
  `ScreenContainer(hasTopBar, hasBottomBar)` applies the matching padding.
- **Auth & networking**: `HttpClientManager` lazily builds one Ktor client with bearer auth. Tokens
  are loaded from DataStore `Settings`; on 401 it calls `auth/refresh`, persists the new pair, or
  runs `LogoutUseCase` (clears settings and navigates to `Graph.Auth`). Call `installAuth()` after
  login to rebuild the client. The start destination is chosen in `MainActivity` based on whether an
  access token exists.
- **Platform services** (`MkLogger`, `OSNavigation`, `DataStore<Settings>`, cache dir,
  `ContentResolver`, location client) are provided in `:app`'s `appModule` so `:shared` stays
  Android-free.

## Git Commit Message

In the repository we use and enforce the commit message conventions.

### Format of the commit message:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

### Example commit message:

```
fix(middleware): ensure Range headers adhere more closely to RFC 2616

Add one new dependency, use `range-parser` (Express dependency) to compute
range. It is more well-tested in the wild.

Fixes #2310
```

### Message subject (first line)

The first line cannot be longer than 72 characters and should be followed by a blank line. The type
and scope should always be lowercase as shown below.

#### Allowed <type> values:

- feat for a new feature for the user, not a new feature for build script. Such commit will trigger
  a release bumping a MINOR version.
- fix for a bug fix for the user, not a fix to a build script. Such commit will trigger a release
  bumping a PATCH version.
- perf for performance improvements. Such commit will trigger a release bumping a PATCH version.
- docs for changes to the documentation.
- style for formatting changes, missing semicolons, etc.
- refactor for refactoring production code, e.g. renaming a variable.
- test for adding missing tests, refactoring tests; no production code change.
- build for updating build configuration, development tools or other changes irrelevant to the user.

#### Example <scope> values:

- init
- runner
- watcher
- config
- web-server
- proxy
- etc.

The <scope> can be empty (e.g. if the change is a global or difficult to assign to a single
component), in which case the parentheses are omitted.

### Message body

Just as in the <subject>, use the imperative, present tense: "change" not "changed" nor "changes".
Message body should include motivation for the change and contrasts with previous behavior.

### Message footer

#### Referencing issues

Closed issues should be listed on a separate line in the footer prefixed with "Closes" keyword like
this:

`Closes #234`

or in the case of multiple issues:

`Closes #123, #245, #992`

#### Breaking changes

All breaking changes have to be mentioned in footer with the description of the change,
justification and migration notes.

```
BREAKING CHANGE:

`port-runner` command line option has changed to `runner-port`, so that it is
consistent with the configuration file syntax.

To migrate your project, change all the commands, where you use `--port-runner`
to `--runner-port`.
```

Any commit with the breaking change section will trigger a MAJOR release and appear on the changelog
independently of the commit type.

